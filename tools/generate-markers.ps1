Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$output = Join-Path $project 'app/src/main/assets/markers'
New-Item -ItemType Directory -Force -Path $output | Out-Null

# Original, reproducible reference cards: asymmetrical high-contrast landmarks,
# unique text and different silhouettes. No external illustrations or color classifier.
function New-Marker([string]$file, [string]$name, [int]$seed, [bool]$rex) {
    $bitmap = [System.Drawing.Bitmap]::new(720, 1000)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Ivory)
    $ink = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(20, 46, 33))
    $pen = [System.Drawing.Pen]::new($ink, 5)
    $font = [System.Drawing.Font]::new('Arial', 34, [System.Drawing.FontStyle]::Bold)
    $small = [System.Drawing.Font]::new('Arial', 19)
    $g.DrawRectangle($pen, 12, 12, 696, 976)
    $g.DrawString('DINOAR / MUSEO', $font, $ink, 38, 35)
    $g.DrawString($name, $font, $ink, 38, 850)
    $g.DrawString('CRETACICO / ' + $seed + ' / COLECCION', $small, $ink, 38, 920)
    $rng = [System.Random]::new($seed)
    for ($i = 0; $i -lt 210; $i++) {
        $x = $rng.Next(35, 660); $y = $rng.Next(125, 805)
        $size = $rng.Next(8, 30)
        if ($i % 3 -eq 0) { $g.DrawEllipse($pen, $x, $y, $size, $size) }
        elseif ($i % 3 -eq 1) { $g.FillRectangle($ink, $x, $y, $size, $size) }
        else { $g.DrawLine($pen, $x, $y, $x + $size, $y + $rng.Next(-28, 29)) }
    }
    $paper = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::Ivory)
    $g.FillEllipse($paper, 110, 315, 500, 300)
    if ($rex) {
        $coords = @(@(115,555),@(240,455),@(340,410),@(425,425),@(445,330),@(550,330),@(580,380),@(550,415),@(485,410),@(460,495),@(400,535),@(385,620),@(435,640),@(345,640),@(340,540),@(295,525),@(260,615),@(290,640),@(215,640),@(235,520))
    } else {
        $coords = @(@(110,515),@(205,455),@(330,425),@(410,450),@(455,370),@(500,415),@(545,385),@(525,460),@(605,490),@(555,510),@(575,540),@(505,545),@(465,515),@(430,555),@(425,630),@(380,630),@(375,555),@(280,555),@(270,630),@(225,630),@(225,530))
    }
    $points = [System.Drawing.Point[]]@($coords | ForEach-Object { [System.Drawing.Point]::new($_[0], $_[1]) })
    $g.FillPolygon($ink, $points)
    $g.FillEllipse($paper, $(if ($rex) { 525 } else { 510 }),  $(if ($rex) { 355 } else { 478 }), 12, 12)
    $bitmap.Save((Join-Path $output $file), [System.Drawing.Imaging.ImageFormat]::Png)
    $font.Dispose(); $small.Dispose(); $pen.Dispose(); $ink.Dispose(); $paper.Dispose(); $g.Dispose(); $bitmap.Dispose()
}
New-Marker 'trex_marker.png' 'TIRANOSAURIO REX' 2718 $true
New-Marker 'triceratops_marker.png' 'TRICERATOPS' 3141 $false
