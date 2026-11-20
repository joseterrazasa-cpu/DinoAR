"""Create reproducible cards from the supplied GLB bind-pose meshes, not generic art.

Run with a Python environment containing NumPy, Pillow and ReportLab.
Original cards and GLB files are never rewritten.
"""
from pathlib import Path
import json
import math
import hashlib
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from glb_geometry import Geometry

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT/'app/src/main/assets/models'
MARKERS = ROOT/'app/src/main/assets/markers'
PREVIEWS = ROOT/'docs/expansion/renders'
ITEMS = [('pachycephalosaurus', 'PACHYCEPHALOSAURUS'), ('mosasaurus', 'MOSASAURUS'),
         ('dimorphodon', 'DIMORPHODON'), ('spinosaurus', 'SPINOSAURUS'), ('ankylosaurus', 'ANKYLOSAURUS')]


def render(vertices, faces, slug):
    center = (vertices.min(axis=0)+vertices.max(axis=0))/2
    v = vertices-center
    # Align the longest horizontal axis with the card; retain Y-up and a slightly elevated view.
    _, axis = np.linalg.eigh(np.cov(v[:, [0, 2]].T))
    horizontal = axis[:, -1]
    right = np.array([horizontal[0], 0, horizontal[1]])
    depth = np.cross(right, [0, 1, 0])
    theta = math.radians(12)
    right, depth = right*math.cos(theta)+depth*math.sin(theta), depth*math.cos(theta)-right*math.sin(theta)
    elevation = math.radians(65 if slug == 'dimorphodon' else 30 if slug == 'mosasaurus' else 16)
    up = np.array([0, 1, 0])*math.cos(elevation)+depth*math.sin(elevation)
    camera = np.cross(right, up)
    projected = np.column_stack([v@right, v@up, v@camera])
    span = np.ptp(projected[:, :2], axis=0)
    scale = min(500/max(span[0], 1e-6), 450/max(span[1], 1e-6))
    xy = projected[:, :2]*[scale, -scale]
    xy -= (xy.min(axis=0)+xy.max(axis=0))/2
    xy += [512, 495]
    normals = np.cross(v[faces[:, 1]]-v[faces[:, 0]], v[faces[:, 2]]-v[faces[:, 0]])
    normals /= np.maximum(np.linalg.norm(normals, axis=1, keepdims=True), 1e-9)
    light = camera*0.7+up*0.6-right*0.3
    shade = np.abs(normals@light)
    canvas = Image.new('RGB', (1024, 1024), 'white')
    draw = ImageDraw.Draw(canvas)
    for f in np.argsort(projected[faces, 2].mean(axis=1)):
        tone = 12 if shade[f] < 0.55 else 225
        draw.polygon([tuple(p) for p in xy[faces[f]]], fill=(tone,)*3)
    return canvas


def main():
    PREVIEWS.mkdir(parents=True, exist_ok=True)
    MARKERS.mkdir(parents=True, exist_ok=True)
    manifest = {}
    font = ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf', 25)
    for number, (slug, label) in enumerate(ITEMS, 4):
        path = MODELS/(slug+'.glb')
        geometry = Geometry(path)
        vertices, faces = geometry.mesh()
        low, high = vertices.min(axis=0), vertices.max(axis=0)
        center, half = (low+high)/2, (high-low)/2
        art = render(vertices, faces, slug)
        art.save(PREVIEWS/(slug+'.png'))
        # Same square format, white paper, thick frame, cut top-left corner and corner fiducials.
        card = Image.new('RGB', (1024, 1024), '#eef1f2')
        draw = ImageDraw.Draw(card)
        draw.rectangle((118, 103, 906, 921), fill='white')
        card.paste(art.crop((232, 232, 777, 773)), (232, 232))
        draw.rectangle((196, 196, 809, 808), outline='black', width=35)
        draw.polygon([(230, 230), (329, 230), (230, 329)], fill='black')
        draw.rectangle((704, 260, 744, 300), fill='black')
        draw.ellipse((709, 265, 739, 295), fill='white', outline='black', width=2)
        draw.line((724, 244, 724, 316), fill='black', width=4)
        draw.line((688, 280, 760, 280), fill='black', width=4)
        draw.line((258, 688, 258, 744, 313, 744), fill='black', width=17)
        draw.ellipse((250, 651, 266, 667), fill='black')
        draw.ellipse((335, 735, 350, 750), fill='black')
        draw.polygon([(636, 752), (657, 716), (677, 752)], fill='black')
        # Distinct corner signatures supplement the different model silhouettes.
        for bit in range(4):
            x = 690+bit*16
            if number & (1 << bit):
                draw.rectangle((x, 709, x+10, 748), fill='black')
            else:
                draw.ellipse((x, 722, x+10, 732), fill='black')
        draw.polygon([(718, 650), (753, 629), (753, 672)], fill='black')
        draw.text((512, 850), label, fill='black', font=font, anchor='mm')
        target = MARKERS/(slug+'_marker.png')
        card.save(target, dpi=(300, 300))
        manifest[slug] = {'center': center.tolist(), 'halfExtent': half.tolist(),
                          'vertices': len(vertices), 'triangles': len(faces),
                          'animationCount': len(geometry.doc.get('animations', [])),
                          'asset': geometry.doc['asset'], 'modelSha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                          'markerSha256': hashlib.sha256(target.read_bytes()).hexdigest()}
        print(slug, len(vertices), 'vertices', len(faces), 'triangles', 'bounds', center, half)
    (ROOT/'docs/expansion/assets.json').write_text(json.dumps(manifest, indent=2, ensure_ascii=False), encoding='utf-8')


if __name__ == '__main__':
    main()
