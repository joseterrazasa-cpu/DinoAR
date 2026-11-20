"""Create real, minimal glTF 2.0 test solids; these are NOT dinosaur models.

Only Python's standard library is needed. Each face has normals and a PBR material.
"""
import json
import math
import struct
from pathlib import Path

OUT = Path(__file__).resolve().parents[1] / 'app/src/main/assets/models'


def write_model(name, faces, color):
    positions, normals, indices = [], [], []
    for face in faces:
        a, b, c = face[:3]
        u, v = [b[i] - a[i] for i in range(3)], [c[i] - a[i] for i in range(3)]
        n = [u[1]*v[2]-u[2]*v[1], u[2]*v[0]-u[0]*v[2], u[0]*v[1]-u[1]*v[0]]
        length = math.sqrt(sum(x*x for x in n))
        n = [x/length for x in n]
        base = len(positions)
        positions.extend(face)
        normals.extend([n]*len(face))
        for i in range(1, len(face)-1):
            indices.extend([base, base+i, base+i+1])
    pos = struct.pack('<'+'f'*len(positions)*3, *(v for p in positions for v in p))
    normal = struct.pack('<'+'f'*len(normals)*3, *(v for p in normals for v in p))
    idx = struct.pack('<'+'H'*len(indices), *indices)
    binary = pos + normal + idx
    model = {
        'asset': {'version': '2.0', 'generator': 'DinoAR test solid generator'},
        'scene': 0, 'scenes': [{'nodes': [0]}],
        'nodes': [{'mesh': 0, 'name': name}],
        'meshes': [{'primitives': [{'attributes': {'POSITION': 0, 'NORMAL': 1}, 'indices': 2, 'material': 0}]}],
        'materials': [{'pbrMetallicRoughness': {'baseColorFactor': color, 'metallicFactor': 0, 'roughnessFactor': 0.65}, 'doubleSided': True}],
        'buffers': [{'byteLength': len(binary)}],
        'bufferViews': [
            {'buffer': 0, 'byteOffset': 0, 'byteLength': len(pos), 'target': 34962},
            {'buffer': 0, 'byteOffset': len(pos), 'byteLength': len(normal), 'target': 34962},
            {'buffer': 0, 'byteOffset': len(pos)+len(normal), 'byteLength': len(idx), 'target': 34963}],
        'accessors': [
            {'bufferView': 0, 'componentType': 5126, 'count': len(positions), 'type': 'VEC3',
             'min': [min(p[i] for p in positions) for i in range(3)], 'max': [max(p[i] for p in positions) for i in range(3)]},
            {'bufferView': 1, 'componentType': 5126, 'count': len(normals), 'type': 'VEC3'},
            {'bufferView': 2, 'componentType': 5123, 'count': len(indices), 'type': 'SCALAR'}]
    }
    js = json.dumps(model, separators=(',', ':')).encode()
    js += b' ' * (-len(js) % 4)
    binary += b'\0' * (-len(binary) % 4)
    data = struct.pack('<4sII', b'glTF', 2, 12+8+len(js)+8+len(binary))
    data += struct.pack('<I4s', len(js), b'JSON') + js
    data += struct.pack('<I4s', len(binary), b'BIN\0') + binary
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_bytes(data)
    print(name, len(data), 'bytes')


write_model('demo_cube.glb', [
    [(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)],
    [(1,-1,-1),(-1,-1,-1),(-1,1,-1),(1,1,-1)],
    [(1,-1,1),(1,-1,-1),(1,1,-1),(1,1,1)],
    [(-1,-1,-1),(-1,-1,1),(-1,1,1),(-1,1,-1)],
    [(-1,1,1),(1,1,1),(1,1,-1),(-1,1,-1)],
    [(-1,-1,-1),(1,-1,-1),(1,-1,1),(-1,-1,1)]
], [0.25,0.7,0.4,1])
write_model('demo_pyramid.glb', [
    [(-1,-1,1),(1,-1,1),(0,1,0)],
    [(1,-1,1),(1,-1,-1),(0,1,0)],
    [(1,-1,-1),(-1,-1,-1),(0,1,0)],
    [(-1,-1,-1),(-1,-1,1),(0,1,0)],
    [(-1,-1,-1),(1,-1,-1),(1,-1,1),(-1,-1,1)]
], [0.85,0.48,0.16,1])
