# pdf-strip-watermark

A simple tool to remove watermarks and other stuff from PDF files.

## Usage

    Arguments required: <input.pdf> <output.pdf> <watermark text> [auto-title]
    
If `auto-title` is given, the title of the final PDF will be changed to
match the name of the input PDF.

This tool is simple and can only remove textual watermarks enclosed between
`q` and `Q` operators of a PDF file.

## Examples

Remove a watermark using a simple text search:

    java -jar pdf-strip-watermark-0.1-all.jar in.pdf out.pdf Confidential
    
Remove a watermark and change title automatically:

    java -jar pdf-strip-watermark-0.1-all.jar in.pdf out.pdf Confidential anytext
    
Remove a watermark if text matches a regular expression:

    java -jar pdf-strip-watermark-0.1-all.jar in.pdf out.pdf 'match:.*Confidential.*'
    
Remove a watermark using a case-insensitive keyword:

    java -jar pdf-strip-watermark-0.1-all.jar in.pdf out.pdf 'match:(?i).*confidential.*'
    
## Download

[pdf-strip-watermark-0.3-all.jar](https://github.com/thebabush/pdf-strip-watermark/blob/master/pdf-strip-watermark-0.3-all.jar)
    
## Building

Using Maven:

    mvn assembly:single

The final `jar` will be inside of the `target/` directory.
