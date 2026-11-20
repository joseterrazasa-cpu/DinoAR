"""Read embedded glTF geometry and evaluate skin bind pose without changing the GLB.

Used only by offline card generation. Requires NumPy from the bundled runtime.
"""
import json
import struct
from pathlib import Path
import numpy as np


class Geometry:
    def __init__(self, path):
        raw = Path(path).read_bytes()
        magic, version, total = struct.unpack_from('<4sII', raw)
        assert magic == b'glTF' and version == 2 and total == len(raw)
        size = struct.unpack_from('<I', raw, 12)[0]
        self.doc = json.loads(raw[20:20+size])
        self.binary = raw[28+size:]
        self.world = {}
        for node in self.doc['scenes'][self.doc.get('scene', 0)]['nodes']:
            self.visit(node, np.eye(4))

    def accessor(self, index):
        a = self.doc['accessors'][index]
        if 'sparse' in a:
            raise ValueError('Sparse accessor requires an explicit decoder')
        view = self.doc['bufferViews'][a['bufferView']]
        size = {'SCALAR': 1, 'VEC2': 2, 'VEC3': 3, 'VEC4': 4, 'MAT4': 16}[a['type']]
        dtype = np.dtype({5120: 'i1', 5121: 'u1', 5122: '<i2', 5123: '<u2', 5125: '<u4', 5126: '<f4'}[a['componentType']])
        array = np.ndarray((a['count'], size), dtype=dtype, buffer=self.binary,
                           offset=view.get('byteOffset', 0)+a.get('byteOffset', 0),
                           strides=(view.get('byteStride', size*dtype.itemsize), dtype.itemsize)).copy()
        if a.get('normalized') and dtype.kind in 'iu':
            array = array.astype(float)/np.iinfo(dtype).max
            if dtype.kind == 'i':
                array = np.maximum(array, -1)
        return array

    def visit(self, index, parent):
        node = self.doc['nodes'][index]
        if 'matrix' in node:
            local = np.array(node['matrix']).reshape(4, 4, order='F')
        else:
            x, y, z, w = node.get('rotation', [0, 0, 0, 1])
            local = np.eye(4)
            local[:3, :3] = [[1-2*(y*y+z*z), 2*(x*y-z*w), 2*(x*z+y*w)],
                              [2*(x*y+z*w), 1-2*(x*x+z*z), 2*(y*z-x*w)],
                              [2*(x*z-y*w), 2*(y*z+x*w), 1-2*(x*x+y*y)]]
            local[:3, :3] *= node.get('scale', [1, 1, 1])
            local[:3, 3] = node.get('translation', [0, 0, 0])
        self.world[index] = parent @ local
        for child in node.get('children', []):
            self.visit(child, self.world[index])

    def mesh(self):
        points, faces = [], []
        offset = 0
        for index, node in enumerate(self.doc['nodes']):
            if 'mesh' not in node or index not in self.world:
                continue
            for primitive in self.doc['meshes'][node['mesh']]['primitives']:
                if primitive.get('mode', 4) != 4:
                    continue
                attributes = primitive['attributes']
                vertices = self.accessor(attributes['POSITION']).astype(float)
                homogeneous = np.column_stack([vertices, np.ones(len(vertices))])
                if 'skin' in node and 'JOINTS_0' in attributes:
                    skin = self.doc['skins'][node['skin']]
                    inverse = self.accessor(skin['inverseBindMatrices']).reshape(-1, 4, 4).transpose(0, 2, 1)
                    bones = np.array([self.world[j] @ m for j, m in zip(skin['joints'], inverse)])
                    joints = self.accessor(attributes['JOINTS_0']).astype(int)
                    weights = self.accessor(attributes['WEIGHTS_0']).astype(float)
                    weights /= np.maximum(weights.sum(axis=1, keepdims=True), 1e-9)
                    transformed = np.einsum('nvij,nj->nvi', bones[joints], homogeneous)
                    vertices = (transformed*weights[:, :, None]).sum(axis=1)[:, :3]
                else:
                    vertices = (homogeneous @ self.world[index].T)[:, :3]
                indices = self.accessor(primitive['indices']).reshape(-1) if 'indices' in primitive else np.arange(len(vertices))
                points.append(vertices)
                faces.append(indices.reshape(-1, 3).astype(int)+offset)
                offset += len(vertices)
        return np.concatenate(points), np.concatenate(faces)
