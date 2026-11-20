"""Export exactly the registered references at 88 mm, plus attribution page."""
from pathlib import Path
import json
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

ROOT = Path(__file__).resolve().parents[1]
ITEMS = [('trex', 'Tiranosaurio Rex', 'jpg'), ('triceratops', 'Triceratops', 'jpg'),
         ('velociraptor', 'Velociraptor', 'jpg'), ('pachycephalosaurus', 'Pachycephalosaurus', 'png'),
         ('mosasaurus', 'Mosasaurus', 'png'), ('dimorphodon', 'Dimorphodon', 'png'),
         ('spinosaurus', 'Spinosaurus', 'png'), ('ankylosaurus', 'Ankylosaurus', 'png')]


def main():
    output = ROOT/'output/pdf'
    output.mkdir(parents=True, exist_ok=True)
    target = output/'DinoAR-tarjetas.pdf'
    c = canvas.Canvas(str(target), pagesize=A4)
    c.setTitle('DinoAR - Tarjetas de reconocimiento')
    c.setAuthor('Proyecto DinoAR; modelos atribuidos en la última página')
    for page in range(2):
        c.setFillColorRGB(.06, .16, .12)
        c.setFont('Helvetica-Bold', 22)
        c.drawString(12*mm, 280*mm, 'DinoAR / Colección prehistórica')
        c.setFont('Helvetica', 10)
        c.drawString(12*mm, 271*mm, 'Imprimir al 100 %. Cada imagen completa mide 88 × 88 mm.')
        for slot, (slug, name, extension) in enumerate(ITEMS[page*4:page*4+4]):
            x = (12+(slot % 2)*98)*mm
            y = (176-(slot//2)*104)*mm
            marker = ROOT/f'app/src/main/assets/markers/{slug}_marker.{extension}'
            c.drawImage(ImageReader(str(marker)), x, y, width=88*mm, height=88*mm)
            c.setFont('Helvetica-Bold', 9)
            c.drawCentredString(x+44*mm, y-5*mm, f'{page*4+slot+1:02} / {name}')
        c.setFont('Helvetica', 10)
        c.drawString(12*mm, 49*mm, 'Usar papel mate, buena luz y una tarjeta a la vez.')
        c.drawString(12*mm, 43*mm, 'Conservar la imagen completa; no recortar el marco ni los detalles.')
        c.drawString(12*mm, 37*mm, 'El detector utiliza estas mismas referencias, sin modificaciones.')
        c.setLineWidth(.6)
        c.line(12*mm, 25*mm, 62*mm, 25*mm)
        c.setFont('Helvetica', 8)
        c.drawString(12*mm, 20*mm, 'Control de escala: esta línea debe medir 50 mm.')
        c.drawRightString(198*mm, 12*mm, f'{page+1} / 3')
        c.showPage()
    c.setFillColorRGB(.06, .16, .12)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(12*mm, 280*mm, 'Créditos de los recursos')
    c.setFont('Helvetica', 10)
    c.drawString(12*mm, 269*mm, 'Las cinco tarjetas nuevas se renderizaron desde los GLB suministrados.')
    c.drawString(12*mm, 262*mm, 'Las tres tarjetas iniciales son imágenes aportadas al proyecto.')
    y = 249*mm
    for slug, name, _ in ITEMS:
        model = ROOT/f'app/src/main/assets/models/{slug}.glb'
        import struct
        raw = model.read_bytes()
        n = struct.unpack_from('<I', raw, 12)[0]
        meta = json.loads(raw[20:20+n])['asset'].get('extras', {})
        c.setFont('Helvetica-Bold', 11)
        c.drawString(12*mm, y, name)
        y -= 6*mm
        c.setFont('Helvetica', 9)
        author = meta.get('author', 'Archivo aportado; autor y licencia no incluidos en sus metadatos.')
        # Long URLs stay intact and receive a clickable link annotation.
        c.drawString(12*mm, y, author)
        y -= 5*mm
        if meta.get('source'):
            url = meta['source']
            c.setFont('Helvetica', 7)
            c.drawString(12*mm, y, url)
            c.linkURL(url, (12*mm, y-2, 198*mm, y+7), relative=0)
            y -= 5*mm
            c.setFont('Helvetica', 8)
            c.drawString(12*mm, y, 'CC BY 4.0 · https://creativecommons.org/licenses/by/4.0/')
            y -= 5*mm
        y -= 5*mm
    c.setFont('Helvetica', 8)
    c.drawString(12*mm, 19*mm, 'GLB sin alterar. Tarjetas nuevas: render monocromático, marco y rotulación añadidos.')
    c.drawRightString(198*mm, 12*mm, '3 / 3')
    c.save()
    print(target)


if __name__ == '__main__':
    main()
