#!/usr/bin/env python3
"""B2C 팀 프로젝트 주제 10선 — 쉬운 순 슬라이드."""

from lxml import etree
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import PP_ALIGN
from pptx.oxml.ns import qn
from pptx.util import Inches, Pt

W, H = 13.333, 7.5
FONT = "Malgun Gothic"
TOTAL = 6

NAVY = RGBColor(0x1B, 0x2E, 0x3C)
INK = RGBColor(0x1C, 0x28, 0x30)
CREAM = RGBColor(0xF3, 0xEF, 0xE7)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
ORANGE = RGBColor(0xE0, 0x6A, 0x2C)
TEAL = RGBColor(0x1C, 0x6B, 0x64)
BLUE = RGBColor(0x2A, 0x4C, 0x78)
MUTED = RGBColor(0x5C, 0x68, 0x70)
LINE = RGBColor(0xDD, 0xD4, 0xC6)
SOFT = RGBColor(0xF7, 0xF5, 0xF1)
EASY_BG = RGBColor(0xE5, 0xF2, 0xEF)
MID_BG = RGBColor(0xFB, 0xF0, 0xE6)
HARD_BG = RGBColor(0xE8, 0xEE, 0xF6)

OUTS = [
    "/workspace/docs/B2C-팀프로젝트-주제10선.pptx",
    "/workspace/docs/b2c-team-project-topics.pptx",
]

# 구현이 쉬운 순. 테이블이 적고 막는 규칙이 하나일수록 앞쪽.
PROJECTS = [
    ("1", "카페 픽업", "MyBatis", "쉬움", TEAL, "메뉴를 고르고, 주문 상태만 바꿉니다.", "메뉴 · 주문 · 상태"),
    ("2", "원데이 클래스", "JPA", "쉬움", TEAL, "회차를 고르고, 정원만 넘지 않게 받습니다.", "클래스 · 회차 · 신청"),
    ("3", "맛집 예약", "MyBatis", "쉬움", TEAL, "식당, 시간, 인원만 받으면 됩니다.", "식당 · 예약"),
    ("4", "펫 용품몰", "MyBatis", "보통", ORANGE, "상품을 담고 주문하면 재고가 줄어듭니다.", "상품 · 장바구니 · 주문"),
    ("5", "온라인 서점", "JPA", "보통", ORANGE, "펫 용품몰과 같고, 리뷰만 더합니다.", "책 · 주문 · 리뷰"),
    ("6", "꽃·식물 구독", "JPA", "보통", ORANGE, "주문 대신, 매달 배송이 반복됩니다.", "플랜 · 회차 · 배송"),
    ("7", "세탁 수거", "API", "보통", ORANGE, "신청하고, 상태를 다음 단계로 옮깁니다.", "신청 · 상태 변경"),
    ("8", "중고 장터", "JPA", "어려움", BLUE, "파는 사람과 사는 사람이 모두 회원입니다.", "판매글 · 거래 상태"),
    ("9", "스터디카페", "MyBatis", "어려움", BLUE, "같은 좌석, 같은 시간은 한 명만 됩니다.", "좌석 · 시간 · 겹침"),
    ("10", "영화 예매", "JPA", "어려움", BLUE, "영화, 상영시간, 좌석이 한꺼번에 연결됩니다.", "영화 · 상영 · 좌석"),
]


def set_run_font(run, name, size, bold, color):
    run.font.name = name
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.color.rgb = color
    run.font.italic = False
    r_pr = run._r.get_or_add_rPr()
    for tag in ("latin", "ea", "cs"):
        el = r_pr.find(qn(f"a:{tag}"))
        if el is None:
            el = etree.SubElement(r_pr, qn(f"a:{tag}"))
        el.set("typeface", name)


def _anchor(tf, anchor):
    tf.word_wrap = True
    tf.auto_size = None
    tf._txBody.bodyPr.set("anchor", {"top": "t", "middle": "ctr", "bottom": "b"}[anchor])


def add_text(slide, x, y, w, h, text, size=16, bold=False, color=INK, align="left", anchor="top"):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    _anchor(tf, anchor)
    p = tf.paragraphs[0]
    p.alignment = {"left": PP_ALIGN.LEFT, "center": PP_ALIGN.CENTER, "right": PP_ALIGN.RIGHT}[align]
    p.space_before = Pt(0)
    p.space_after = Pt(0)
    run = p.add_run()
    run.text = text
    set_run_font(run, FONT, size, bold, color)
    return box


def rect(slide, x, y, w, h, fill, line=None, radius=None):
    kind = MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE
    sh = slide.shapes.add_shape(kind, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid()
    sh.fill.fore_color.rgb = fill
    if line is None:
        sh.line.fill.background()
    else:
        sh.line.color.rgb = line
        sh.line.width = Pt(1)
    if radius is not None:
        try:
            sh.adjustments[0] = radius
        except Exception:
            pass
    return sh


def bg(slide):
    rect(slide, 0, 0, W, H, CREAM)


def footer(slide, page):
    add_text(slide, 0.48, 7.16, 8.5, 0.26, "쉬운 순  ·  1번부터 고르면 됩니다", size=12, color=MUTED)
    add_text(slide, 11.15, 7.16, 1.7, 0.26, f"{page}  /  {TOTAL}", size=12, color=MUTED, align="right")


def kicker(slide, text):
    rect(slide, 0.48, 0.36, 0.28, 0.1, ORANGE, radius=0.5)
    add_text(slide, 0.88, 0.26, 8, 0.3, text, size=14, bold=True, color=ORANGE, anchor="middle")


def title(slide, text):
    add_text(slide, 0.48, 0.52, 12.3, 0.58, text, size=32, bold=True, color=NAVY)


def slide_cover(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    rect(s, 0, 0, 0.22, H, ORANGE)
    rect(s, 0.5, 0.4, 7.55, 6.7, NAVY, radius=0.08)

    add_text(s, 0.95, 0.78, 6.6, 0.34, "SPRING BOOT  TEAM PROJECT", size=14, bold=True, color=RGBColor(0xE7, 0xA0, 0x72))
    add_text(s, 0.95, 1.45, 6.7, 0.9, "1번이 가장 쉽습니다", size=22, color=RGBColor(0xD5, 0xDE, 0xE4))
    add_text(s, 0.95, 2.25, 6.8, 1.5, "쉬운 순으로 보는\nB2C 주제 10개", size=36, bold=True, color=WHITE)
    add_text(s, 0.95, 4.55, 6.6, 1.1, "위에서부터 고르면 됩니다.\n테이블이 적고, 규칙이 하나인 주제가 쉽습니다.", size=16, color=RGBColor(0xC5, 0xD0, 0xD6))

    add_text(s, 8.4, 0.48, 4.4, 0.4, "쉬운 순", size=16, bold=True, color=NAVY)
    for i, p in enumerate(PROJECTS):
        num, name, _stack, level, color, _why, _make = p
        y = 1.02 + i * 0.58
        rect(s, 8.4, y, 4.45, 0.5, WHITE, line=LINE, radius=0.15)
        add_text(s, 8.52, y, 0.55, 0.5, num, size=16, bold=True, color=color, align="center", anchor="middle")
        add_text(s, 9.1, y, 2.35, 0.5, name, size=15, bold=True, color=INK, anchor="middle")
        add_text(s, 11.35, y, 1.35, 0.5, level, size=13, bold=True, color=color, align="right", anchor="middle")


def slide_rules(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "순서의 기준")
    title(s, "쉬운 주제는 조건이 세 가지입니다")
    add_text(s, 0.48, 1.2, 12, 0.4, "이 세 가지가 맞을수록 앞 번호입니다.", size=18, color=MUTED)

    cards = [
        (TEAL, "1", "테이블이 적다", "회원, 대상, 신청.\n이 세 개면 충분합니다.", "카페 픽업은 메뉴와 주문뿐입니다."),
        (ORANGE, "2", "막는 규칙이 하나다", "재고이거나, 정원이거나,\n상태 변경 중 하나입니다.", "클래스는 정원만 막으면 됩니다."),
        (BLUE, "3", "화면이 한 줄이다", "목록 → 신청 → 내 내역.\n관리자는 등록만 합니다.", "결제는 ‘주문 완료’로 충분합니다."),
    ]
    for i, (color, num, head, body, note) in enumerate(cards):
        x = 0.48 + i * 4.2
        rect(s, x, 1.9, 4.0, 4.7, WHITE, line=LINE, radius=0.08)
        rect(s, x, 1.9, 4.0, 0.12, color)
        rect(s, x + 0.28, 2.25, 0.62, 0.62, color, radius=0.3)
        add_text(s, x + 0.28, 2.25, 0.62, 0.62, num, size=20, bold=True, color=WHITE, align="center", anchor="middle")
        add_text(s, x + 1.05, 2.28, 2.7, 0.58, head, size=22, bold=True, color=NAVY, anchor="middle")
        add_text(s, x + 0.32, 3.2, 3.4, 1.6, body, size=18, color=INK)
        rect(s, x + 0.28, 5.15, 3.44, 1.1, SOFT, radius=0.12)
        add_text(s, x + 0.46, 5.15, 3.1, 1.1, note, size=15, bold=True, color=NAVY, anchor="middle")
    footer(s, 2)


def rank_row(slide, y, project):
    num, name, stack, level, color, why, make = project
    rect(slide, 0.48, y, 12.36, 1.02, WHITE, line=LINE, radius=0.1)
    rect(slide, 0.48, y, 0.12, 1.02, color)
    add_text(slide, 0.75, y, 0.7, 1.02, num, size=26, bold=True, color=color, anchor="middle")
    add_text(slide, 1.5, y + 0.1, 5.4, 0.46, name, size=22, bold=True, color=NAVY, anchor="middle")
    add_text(slide, 1.5, y + 0.52, 7.3, 0.4, why, size=15, color=INK, anchor="middle")
    add_text(slide, 8.7, y + 0.14, 2.3, 0.36, make, size=13, color=MUTED, align="right")
    rect(slide, 11.15, y + 0.3, 1.4, 0.42, color, radius=0.4)
    add_text(slide, 11.15, y + 0.3, 1.4, 0.42, stack, size=13, bold=True, color=WHITE, align="center", anchor="middle")


def slide_ranks(prs, page, heading, kicker_text, projects):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, kicker_text)
    title(s, heading)
    y = 1.4
    for project in projects:
        rank_row(s, y, project)
        y += 1.12
    footer(s, page)


def slide_pick(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "고르기")
    title(s, "처음이면 이 중에서 고르면 됩니다")

    picks = [
        ("1", "카페 픽업", "가장 짧게 끝냅니다.", "메뉴를 보여주고, 주문 상태를 바꿉니다.", TEAL),
        ("2", "원데이 클래스", "예약을 해보고 싶을 때.", "정원 숫자만 넘지 않게 막습니다.", TEAL),
        ("4", "펫 용품몰", "쇼핑몰을 해보고 싶을 때.", "장바구니와 재고만 있으면 됩니다.", ORANGE),
    ]
    for i, (num, name, when, how, color) in enumerate(picks):
        x = 0.48 + i * 4.2
        rect(s, x, 1.55, 4.0, 3.35, WHITE, line=LINE, radius=0.08)
        rect(s, x, 1.55, 4.0, 0.12, color)
        add_text(s, x + 0.28, 1.85, 1.2, 0.7, num, size=32, bold=True, color=color)
        add_text(s, x + 0.28, 2.6, 3.45, 0.5, name, size=24, bold=True, color=NAVY)
        add_text(s, x + 0.28, 3.25, 3.45, 0.45, when, size=16, bold=True, color=color)
        add_text(s, x + 0.28, 3.8, 3.45, 0.8, how, size=16, color=INK)

    add_text(s, 0.48, 5.15, 12, 0.4, "역할은 항상 같습니다", size=20, bold=True, color=NAVY)
    roles = [
        ("A", "목록", "회원, 상품, 식당, 클래스를 등록하고 보여 줍니다."),
        ("B", "신청", "주문이나 예약을 넣고, 규칙 하나를 검사합니다."),
        ("C", "내역", "나의 신청과 관리자 목록을 보여 줍니다."),
    ]
    for i, (mark, name, body) in enumerate(roles):
        x = 0.48 + i * 4.2
        rect(s, x, 5.65, 4.0, 1.25, WHITE, line=LINE, radius=0.1)
        rect(s, x + 0.16, 5.9, 0.72, 0.72, NAVY, radius=0.25)
        add_text(s, x + 0.16, 5.9, 0.72, 0.72, mark, size=18, bold=True, color=WHITE, align="center", anchor="middle")
        add_text(s, x + 1.05, 5.78, 2.75, 0.4, name, size=16, bold=True, color=NAVY, anchor="middle")
        add_text(s, x + 1.05, 6.18, 2.8, 0.58, body, size=12, color=INK)
    footer(s, 5)


def slide_later(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "뒤쪽 번호")
    title(s, "8번부터는 규칙이 하나 더 있습니다")
    add_text(s, 0.48, 1.22, 12, 0.4, "앞의 7개를 이해한 다음에 고르면 됩니다.", size=18, color=MUTED)

    rows = [
        ("8", "중고 장터", "회원이 양쪽입니다.", "판매 글과 구매가 서로 다른 사람입니다. 상태도 판매중, 예약, 완료로 나뉩니다.", BLUE),
        ("9", "스터디카페", "시간이 겹치면 안 됩니다.", "좌석과 시간을 같이 저장하고, 이미 잡힌 자리는 거절해야 합니다.", BLUE),
        ("10", "영화 예매", "연결이 세 단계입니다.", "영화 아래에 상영이 있고, 상영 아래에 좌석이 있습니다.", BLUE),
    ]
    y = 1.85
    for num, name, point, why, color in rows:
        rect(s, 0.48, y, 12.36, 1.55, WHITE, line=LINE, radius=0.08)
        rect(s, 0.48, y, 0.12, 1.55, color)
        add_text(s, 0.8, y, 0.9, 1.55, num, size=28, bold=True, color=color, anchor="middle")
        add_text(s, 1.85, y + 0.22, 6, 0.45, name, size=24, bold=True, color=NAVY)
        add_text(s, 8.2, y + 0.28, 4.3, 0.4, point, size=16, bold=True, color=color, align="right")
        add_text(s, 1.85, y + 0.78, 10.5, 0.5, why, size=16, color=INK)
        y += 1.7
    footer(s, 6)


def build():
    prs = Presentation()
    prs.slide_width = Inches(W)
    prs.slide_height = Inches(H)
    prs.core_properties.title = "쉬운 순으로 보는 B2C 주제 10개"
    prs.core_properties.subject = "Spring Boot MyBatis JPA API"
    prs.core_properties.author = "팀 프로젝트 가이드"
    slide_cover(prs)
    slide_rules(prs)
    slide_ranks(prs, 3, "1번부터 5번, 먼저 보세요", "쉬운 순  1–5", PROJECTS[:5])
    slide_ranks(prs, 4, "6번부터 10번, 그다음입니다", "쉬운 순  6–10", PROJECTS[5:])
    slide_pick(prs)
    slide_later(prs)
    for path in OUTS:
        prs.save(path)
        print(path)


if __name__ == "__main__":
    build()
