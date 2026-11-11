"""Flatten Stonecutter-generated sources for one version: drop //? markers and inactive (/* */-wrapped) branches.

Usage: python -I strip_stonecutter.py <generated_root> <out_root>
Reports any branch it can't classify so it can be checked by hand.
"""
import os
import re
import sys

MARKER = re.compile(r'(?:(?<=\*/)|^[ \t]*)//\?[^\n]*', re.M)


def classify(body):
    s = body.strip()
    if not s:
        return 'empty'
    if s.startswith('/*') and not s.startswith('/**') and s.endswith('*/'):
        return 'inactive'
    if s.startswith('/*') and not s.startswith('/**'):
        return 'odd'
    if s.endswith('*/') and not s.endswith('**/') and '/*' not in s:
        return 'odd'
    return 'active'


def first_comment_end(text, start):
    # text[start:] begins (after whitespace) with /*: return index just past its */
    i = text.index('/*', start)
    j = text.index('*/', i + 2)
    return j + 2


def strip(text, path, problems):
    out = []
    pos = 0
    markers = list(MARKER.finditer(text))
    if not markers:
        return text
    for k, m in enumerate(markers):
        if m.start() < pos:
            continue  # inside an inactive region already dropped (nested, /^ ^/-escaped markers)
        mtext = m.group(0).strip()
        # Text before this marker (from the previous position) is a body of the previous marker, already handled.
        seg_start = pos
        out.append(('keep', text[seg_start:m.start()]))
        # Drop the marker itself (and its newline if it stood alone on its line).
        end = m.end()
        if end < len(text) and text[end] == '\n':
            end += 1
        pos = end
        is_block = mtext.endswith('{')
        is_close_only = mtext == '//?}'
        if is_close_only:
            continue
        nxt = markers[k + 1].start() if k + 1 < len(markers) else len(text)
        lead = text[pos:nxt].lstrip()
        if is_block and lead.startswith('/*') and not lead.startswith('/**') and not classify(text[pos:nxt]) == 'inactive':
            # Inactive block with nested (escaped) conditionals: it runs to the first real */.
            end_c = first_comment_end(text, pos)
            following = [mm for mm in markers[k + 1:] if mm.start() >= end_c]
            if not following or text[end_c:following[0].start()].strip():
                problems.append(f'{path}: nested inactive block after "{mtext}" not followed by a marker')
            pos = end_c
            continue
        if is_block:
            body = text[pos:nxt]
            kind = classify(body)
            if kind == 'inactive':
                pos = nxt
            elif kind == 'odd':
                problems.append(f'{path}: unclassifiable block after "{mtext}": {body.strip()[:80]!r}')
        else:
            # Single-statement condition: the next statement.
            rest = text[pos:nxt]
            stripped = rest.lstrip()
            if stripped.startswith('/*') and not stripped.startswith('/**'):
                pos = first_comment_end(text, pos)
                if pos < len(text) and text[pos] == '\n':
                    pos += 1
            elif stripped.startswith('//'):
                # Stonecutter's inactive single line: a // line comment.
                nl = text.find('\n', pos + (len(rest) - len(stripped)))
                pos = len(text) if nl < 0 else nl + 1
            elif stripped.startswith('/**'):
                problems.append(f'{path}: single-line condition before javadoc after "{mtext}"')
    out.append(('keep', text[pos:]))
    result = ''.join(t for _, t in out)
    # Remove lines left with only whitespace where a marker sat at the end of code (e.g. "code */" never happens) and
    # trailing whitespace on lines.
    result = re.sub(r'[ \t]+\n', '\n', result)
    # Collapse 3+ blank lines that removal may leave.
    result = re.sub(r'\n{3,}', '\n\n', result)
    return result


def main(src, dst):
    problems = []
    for root, _, files in os.walk(src):
        for f in files:
            p = os.path.join(root, f)
            rel = os.path.relpath(p, src)
            if not f.endswith('.java'):
                q = os.path.join(dst, rel)
                os.makedirs(os.path.dirname(q), exist_ok=True)
                with open(p, 'rb') as a, open(q, 'wb') as b:
                    b.write(a.read())
                continue
            with open(p, 'r', encoding='utf-8', newline='') as fh:
                text = fh.read()
            text = text.replace('\r\n', '\n')
            new = strip(text, rel, problems) if f.endswith('.java') else text
            if '//?' in new:
                problems.append(f'{rel}: marker left')
            if f.endswith('.java') and not re.search(r'\b(class|interface|enum|record)\b', re.sub(r'/\*.*?\*/', '', new, flags=re.S)):
                print(f'EMPTY (whole-file inactive): {rel}')
                continue
            q = os.path.join(dst, rel)
            os.makedirs(os.path.dirname(q), exist_ok=True)
            with open(q, 'w', encoding='utf-8', newline='\n') as fh:
                fh.write(new)
    for pr in problems:
        print('PROBLEM', pr)
    print(f'{len(problems)} problems')


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
