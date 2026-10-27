#!/usr/bin/env python3
"""B2C 팀 프로젝트 주제 10선 — 발표용 슬라이드."""

from lxml import etree
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.oxml.ns import qn
from pptx.util import Emu, Inches, Pt

W, H = 13.333, 7.5
FONT = "Malgun Gothic"

NAVY = RGBColor(0x1B, 0x2E, 0x3C)
NAVY_2 = RGBColor(0x24, 0x40, 0x52)
INK = RGBColor(0x1C, 0x28, 0x30)
CREAM = RGBColor(0xF3, 0xEF, 0xE7)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
ORANGE = RGBColor(0xE0, 0x6A, 0x2C)
TEAL = RGBColor(0x1C, 0x6B, 0x64)
BLUE = RGBColor(0x2A, 0x4C, 0x78)
SAND = RGBColor(0xE7, 0xE0, 0xD4)
MUTED = RGBColor(0x5C, 0x68, 0x70)
LINE = RGBColor(0xDD, 0xD4, 0xC6)
SOFT = RGBColor(0xF7, 0xF5, 0xF1)

SHOP, BOOK, SVC = ORANGE, TEAL, BLUE

OUT = "/workspace/docs/B2C-팀프로젝트-주제10선.pptx"


def rgb_hex(c: RGBColor) -> str:
    return str(c)


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
    mapping = {"top": MSO_ANCHOR.TOP, "middle": MSO_ANCHOR.MIDDLE, "bottom": MSO_ANCHOR.BOTTOM}
    tf.paragraphs[0].alignment  # touch
    body_pr = tf._txBody.bodyPr
    body_pr.set("anchor", {"top": "t", "middle": "ctr", "bottom": "b"}[anchor])


def add_text(slide, x, y, w, h, text, size=16, bold=False, color=INK, align="left", anchor="top", font=FONT):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    _anchor(tf, anchor)
    p = tf.paragraphs[0]
    p.alignment = {"left": PP_ALIGN.LEFT, "center": PP_ALIGN.CENTER, "right": PP_ALIGN.RIGHT}[align]
    p.space_before = Pt(0)
    p.space_after = Pt(0)
    run = p.add_run()
    run.text = text
    set_run_font(run, font, size, bold, color)
    return box


def add_lines(slide, x, y, w, h, lines, size=15, color=INK, bold=False, align="left", anchor="top", spacing=6):
    """lines: list of (text, size, bold, color) or strings."""
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    _anchor(tf, anchor)
    for i, line in enumerate(lines):
        if isinstance(line, str):
            text, sz, b, col = line, size, bold, color
        else:
            text, sz, b, col = line
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = {"left": PP_ALIGN.LEFT, "center": PP_ALIGN.CENTER, "right": PP_ALIGN.RIGHT}[align]
        p.space_before = Pt(0 if i == 0 else spacing)
        p.space_after = Pt(0)
        run = p.add_run()
        run.text = text
        set_run_font(run, FONT, sz, b, col)
    return box


def rect(slide, x, y, w, h, fill, line=None, radius=None):
    shape_type = MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE
    sh = slide.shapes.add_shape(shape_type, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid()
    sh.fill.fore_color.rgb = fill
    if line is None:
        sh.line.fill.background()
    else:
        sh.line.color.rgb = line
        sh.line.width = Pt(1)
    if radius is not None:
        # 0~1 of half the shorter side; keep corners gentle
        try:
            sh.adjustments[0] = radius
        except Exception:
            pass
    # no shadow noise
    sp_pr = sh._element.spPr
    effect = sp_pr.find(qn("a:effectLst"))
    if effect is not None:
        sp_pr.remove(effect)
    return sh


def pill(slide, x, y, w, h, text, fill, text_color=WHITE, size=12):
    rect(slide, x, y, w, h, fill, radius=0.5)
    add_text(slide, x, y, w, h, text, size=size, bold=True, color=text_color, align="center", anchor="middle")


def bg(slide, color=CREAM):
    rect(slide, 0, 0, W, H, color)


def footer(slide, page, total=9):
    add_text(slide, 0.48, 7.18, 8, 0.24, "스프링 부트 팀 프로젝트  ·  B2C 상품·서비스", size=11, color=MUTED)
    add_text(slide, 11.2, 7.18, 1.65, 0.24, f"{page}  /  {total}", size=11, color=MUTED, align="right")


def kicker(slide, text):
    rect(slide, 0.48, 0.32, 0.28, 0.1, ORANGE, radius=0.5)
    add_text(slide, 0.86, 0.22, 8, 0.3, text, size=13, bold=True, color=ORANGE, anchor="middle")


def title(slide, text, y=0.48):
    add_text(slide, 0.48, y, 12.2, 0.52, text, size=30, bold=True, color=NAVY)


# ---------------------------------------------------------------------------
# slides
# ---------------------------------------------------------------------------

def slide_cover(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    rect(s, 0, 0, 0.22, H, ORANGE)
    rect(s, 0.48, 0.38, 8.15, 6.74, NAVY, radius=0.08)

    add_text(s, 0.9, 0.68, 7.2, 0.32, "SPRING BOOT   ·   TEAM PROJECT", size=13, bold=True, color=RGBColor(0xE7, 0xA0, 0x72))
    add_lines(
        s, 0.9, 1.2, 7.3, 2.2,
        [
            ("바로 코딩하는", 36, True, WHITE),
            ("B2C 주제 10가지", 40, True, WHITE),
        ],
        spacing=2,
    )
    add_text(
        s, 0.9, 3.55, 7.0, 0.9,
        "회원, 상품, 주문만 있어도 데모가 됩니다.\nMyBatis · JPA · REST API 중에서 골라 시작하면 됩니다.",
        size=16, color=RGBColor(0xD5, 0xDE, 0xE4),
    )

    pill(s, 0.9, 5.05, 1.55, 0.4, "쇼핑 3", ORANGE, size=13)
    pill(s, 2.58, 5.05, 1.55, 0.4, "예약 4", TEAL, size=13)
    pill(s, 4.26, 5.05, 1.7, 0.4, "서비스 3", BLUE, size=13)
    add_text(s, 0.9, 5.65, 7.0, 0.7, "팀원 3~4명  ·  한 학기 분량  ·  결제는 ‘주문 완료’로 처리", size=14, color=RGBColor(0xB7, 0xC4, 0xCC))

    # right index
    add_text(s, 8.9, 0.48, 4, 0.34, "이 자료의 순서", size=14, bold=True, color=NAVY)
    items = [
        ("01", "한눈에 보는 10개"),
        ("02", "쇼핑 · 예약 · 서비스"),
        ("03", "주제별 넣을 기능"),
        ("04", "스택 고르는 법"),
        ("05", "역할 나누기와 추천"),
    ]
    y = 1.05
    for num, label in items:
        rect(s, 8.9, y, 3.95, 0.95, WHITE, line=LINE, radius=0.12)
        add_text(s, 9.1, y, 0.7, 0.95, num, size=18, bold=True, color=ORANGE, anchor="middle")
        add_text(s, 9.8, y, 2.85, 0.95, label, size=16, bold=True, color=INK, anchor="middle")
        y += 1.08


def slide_criteria(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "고르는 기준")
    title(s, "쉬운 주제는 구조가 같습니다")
    add_text(s, 0.48, 1.12, 12, 0.36, "아래 세 가지를 지키면 기능이 늘어도 팀이 헤매지 않습니다.", size=16, color=MUTED)

    cards = [
        (ORANGE, "01", "도메인 3개", "회원\n상품 또는 예약 대상\n주문 또는 예약", "예: 회원 · 책 · 주문"),
        (TEAL, "02", "화면 5개", "목록\n상세\n담기 · 신청\n내 내역\n관리자", "이 다섯 화면이면 발표가 됩니다"),
        (BLUE, "03", "2차 기능", "실제 카드 결제\n지도 · 배송 조회\n채팅\n알림톡", "결제는 ‘주문 완료’ 상태로 충분"),
    ]
    x = 0.48
    for color, num, head, body, note in cards:
        rect(s, x, 1.7, 4.0, 5.05, WHITE, line=LINE, radius=0.06)
        rect(s, x, 1.7, 4.0, 0.1, color)
        add_text(s, x + 0.28, 1.95, 3.4, 0.32, num, size=13, bold=True, color=color)
        add_text(s, x + 0.28, 2.28, 3.45, 0.7, head, size=24, bold=True, color=NAVY)
        add_text(s, x + 0.28, 3.1, 3.45, 2.35, body, size=18, color=INK)
        rect(s, x + 0.22, 5.55, 3.56, 0.95, SOFT, radius=0.12)
        add_text(s, x + 0.4, 5.55, 3.2, 0.95, note, size=14, bold=True, color=NAVY, anchor="middle")
        x += 4.18
    footer(s, 2)


def slide_map(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "전체 지도")
    title(s, "주제 10개를 한 화면에서")

    items = [
        ("01", "온라인 서점", "책을 고르고 주문", "JPA", SHOP),
        ("02", "카페 픽업", "주문 후 매장 수령", "MyBatis", SVC),
        ("03", "중고 장터", "개인 물품을 거래", "JPA", SHOP),
        ("04", "스터디카페", "시간대 좌석 예약", "MyBatis", BOOK),
        ("05", "영화 예매", "상영과 좌석 선택", "JPA", BOOK),
        ("06", "원데이 클래스", "회차·정원 신청", "JPA", BOOK),
        ("07", "펫 용품몰", "카테고리별 판매", "MyBatis", SHOP),
        ("08", "꽃·식물 구독", "매달 배송 구독", "JPA", SVC),
        ("09", "세탁 수거", "시간 정해 맡기기", "API", SVC),
        ("10", "맛집 예약", "식당·인원 예약", "MyBatis", BOOK),
    ]
    gap_x, gap_y = 0.16, 0.16
    card_w = (W - 0.96 - gap_x * 4) / 5
    card_h = 2.45
    for i, (num, name, desc, stack, color) in enumerate(items):
        col, row = i % 5, i // 5
        x = 0.48 + col * (card_w + gap_x)
        y = 1.55 + row * (card_h + gap_y)
        rect(s, x, y, card_w, card_h, WHITE, line=LINE, radius=0.1)
        rect(s, x, y, card_w, 0.08, color)
        add_text(s, x + 0.14, y + 0.22, card_w - 0.28, 0.36, num, size=13, bold=True, color=color)
        add_text(s, x + 0.14, y + 0.58, card_w - 0.28, 0.7, name, size=18, bold=True, color=NAVY)
        add_text(s, x + 0.14, y + 1.28, card_w - 0.28, 0.5, desc, size=13, color=MUTED)
        pill(s, x + 0.14, y + 1.88, 1.15, 0.34, stack, color, size=11)
    footer(s, 3)


def slide_types(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "세 갈래")
    title(s, "쇼핑, 예약, 서비스로 나뉩니다")

    cols = [
        (SHOP, "쇼핑", "물건을 담고 주문", ["01  온라인 서점", "03  중고 장터", "07  펫 용품몰"], "장바구니와 재고가 핵심"),
        (BOOK, "예약", "시간과 자리를 선점", ["04  스터디카페", "05  영화 예매", "06  원데이 클래스", "10  맛집 예약"], "중복 예약만 막으면 됨"),
        (SVC, "서비스", "신청하고 상태가 흐름", ["02  카페 픽업", "08  꽃·식물 구독", "09  세탁 수거"], "접수 → 진행 → 완료"),
    ]
    x = 0.48
    for color, name, sub, rows, foot in cols:
        rect(s, x, 1.5, 4.0, 5.2, WHITE, line=LINE, radius=0.06)
        rect(s, x, 1.5, 0.1, 5.2, color)
        add_text(s, x + 0.35, 1.7, 3.4, 0.5, name, size=28, bold=True, color=color)
        add_text(s, x + 0.35, 2.25, 3.4, 0.4, sub, size=15, color=MUTED)
        yy = 2.85
        for row in rows:
            rect(s, x + 0.35, yy, 3.35, 0.52, SOFT, radius=0.15)
            add_text(s, x + 0.5, yy, 3.1, 0.52, row, size=15, bold=True, color=INK, anchor="middle")
            yy += 0.64
        add_text(s, x + 0.35, 6.15, 3.4, 0.35, foot, size=13, bold=True, color=color)
        x += 4.18
    footer(s, 4)


def feature_card(slide, x, y, w, h, num, name, stack, color, summary, features, note=None):
    rect(slide, x, y, w, h, WHITE, line=LINE, radius=0.07)
    rect(slide, x, y, w, 0.7, color)
    add_text(slide, x + 0.2, y, 0.7, 0.7, num, size=18, bold=True, color=WHITE, anchor="middle")
    add_text(slide, x + 0.85, y, w - 2.3, 0.7, name, size=18, bold=True, color=WHITE, anchor="middle")
    add_text(slide, x + w - 1.45, y, 1.25, 0.7, stack, size=12, bold=True, color=WHITE, align="right", anchor="middle")
    add_text(slide, x + 0.22, y + 0.84, w - 0.44, 0.55, summary, size=13, color=MUTED)
    yy = y + 1.42
    for feat in features:
        rect(slide, x + 0.22, yy + 0.08, 0.12, 0.12, color, radius=0.5)
        add_text(slide, x + 0.46, yy - 0.06, w - 0.7, 0.36, feat, size=14, color=INK, anchor="middle")
        yy += 0.38
    if note and h >= 4.2:
        rect(slide, x + 0.22, y + h - 1.2, w - 0.44, 0.98, SOFT, radius=0.12)
        add_text(slide, x + 0.4, y + h - 1.16, w - 0.8, 0.3, "이렇게 시작하면 됩니다", size=12, bold=True, color=color)
        add_text(slide, x + 0.4, y + h - 0.84, w - 0.8, 0.52, note, size=14, bold=True, color=NAVY)


def slide_shop(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "쇼핑")
    title(s, "담고, 주문하고, 재고가 줄어듭니다")
    cards = [
        ("01", "온라인 서점", "JPA", "카테고리로 책을 보고 주문합니다.", ["도서·카테고리 등록", "검색, 상세, 리뷰", "장바구니와 주문", "나의 주문 내역"], "책, 주문, 주문상품이면 됩니다."),
        ("03", "중고 장터", "JPA", "회원이 물건을 올리고 다른 회원이 삽니다.", ["판매글과 사진", "찜하기", "판매중 · 예약 · 완료", "내 판매와 내 구매"], "판매 상태만 바꿔도 거래가 보입니다."),
        ("07", "펫 용품몰", "MyBatis", "사료와 용품을 카테고리로 팝니다.", ["상품과 재고", "카테고리 필터", "장바구니와 주문", "관리자 매출 조회"], "상품 목록과 재고 수량만 있으면 됩니다."),
    ]
    cw = 4.0
    for i, (num, name, stack, summary, feats, note) in enumerate(cards):
        feature_card(s, 0.48 + i * 4.18, 1.48, cw, 5.25, num, name, stack, SHOP, summary, feats, note)
    footer(s, 5)


def slide_book(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "예약")
    title(s, "같은 자리, 같은 시간은 한 팀만")
    cards = [
        ("04", "스터디카페", "MyBatis", "날짜·시간을 고르고 좌석을 잡습니다.", ["좌석 배치", "1시간 단위 슬롯", "겹치는 예약 거절", "나의 이용 내역"]),
        ("05", "영화 예매", "JPA", "영화, 상영, 좌석 순서로 예매합니다.", ["영화와 상영시간", "좌석 선택", "예매와 취소", "예매 번호 조회"]),
        ("06", "원데이 클래스", "JPA", "회차와 정원을 보고 신청합니다.", ["클래스 소개", "회차와 정원", "신청과 취소", "수강 후기"]),
        ("10", "맛집 예약", "MyBatis", "식당, 인원, 시간을 예약합니다.", ["식당과 메뉴", "영업시간", "인원 예약", "방문 리뷰"]),
    ]
    positions = [(0.4, 1.42), (6.86, 1.42), (0.4, 4.22), (6.86, 4.22)]
    for (x, y), card in zip(positions, cards):
        num, name, stack, summary, feats = card
        feature_card(s, x, y, 6.08, 2.68, num, name, stack, BOOK, summary, feats[:4] and [
            feats[0] + "   ·   " + feats[1],
            feats[2] + "   ·   " + feats[3],
        ])
    footer(s, 6)


def slide_service(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "서비스")
    title(s, "신청 한 번, 그 다음엔 상태")
    cards = [
        ("02", "카페 픽업", "MyBatis", "메뉴와 옵션을 고르고 매장에서 받습니다.", ["메뉴와 옵션(샷·사이즈)", "주문 넣기", "접수 → 제조 → 완료", "오늘 주문 현황"], "메뉴, 주문, 제조 상태 순서로 만듭니다."),
        ("08", "꽃·식물 구독", "JPA", "플랜을 고르면 매달 배송됩니다.", ["구독 플랜", "배송지 관리", "이번 회차 건너뛰기", "배송 이력"], "플랜 하나와 배송 회차만 있으면 됩니다."),
        ("09", "세탁 수거", "API", "주소와 시간을 정해 세탁을 맡깁니다.", ["요금표 조회", "수거 신청", "수거 → 세탁 → 배송", "상태 변경 API"], "신청 생성과 상태 변경이 본체입니다."),
    ]
    for i, (num, name, stack, summary, feats, note) in enumerate(cards):
        feature_card(s, 0.48 + i * 4.18, 1.48, 4.0, 5.25, num, name, stack, SVC, summary, feats, note)
    footer(s, 7)


def slide_stack(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "기술 선택")
    title(s, "무엇을 많이 다루느냐로 고릅니다")

    cols = [
        (TEAL, "JPA", "관계", ["회원 – 주문 – 주문상품", "상영 – 좌석 – 예매", "클래스 – 회차 – 신청", "구독 – 회차 – 배송"], "서점 · 중고 · 영화\n클래스 · 구독"),
        (ORANGE, "MyBatis", "조회 SQL", ["시간대가 겹치는 좌석", "카테고리·재고 목록", "날짜별 예약 인원", "오늘 매출 합계"], "카페 · 스터디카페\n펫용품 · 맛집"),
        (BLUE, "REST API", "상태 변경", ["신청 생성", "상태 한 단계 이동", "내 신청 목록", "관리자 상태 변경"], "세탁 수거\n화면은 단순하게"),
    ]
    x = 0.48
    for color, name, focus, rows, fit in cols:
        rect(s, x, 1.48, 4.0, 5.22, WHITE, line=LINE, radius=0.06)
        rect(s, x, 1.48, 4.0, 1.15, color)
        add_text(s, x + 0.28, 1.55, 3.4, 0.55, name, size=26, bold=True, color=WHITE)
        add_text(s, x + 0.28, 2.1, 3.4, 0.38, f"중심은 {focus}", size=14, color=WHITE)
        yy = 2.85
        for row in rows:
            add_text(s, x + 0.28, yy, 3.45, 0.38, "·  " + row, size=15, color=INK)
            yy += 0.42
        rect(s, x + 0.28, 4.7, 3.44, 1.55, SOFT, radius=0.1)
        add_text(s, x + 0.46, 4.82, 3.1, 0.3, "잘 맞는 주제", size=12, bold=True, color=color)
        add_text(s, x + 0.46, 5.15, 3.1, 0.9, fit, size=16, bold=True, color=NAVY)
        x += 4.18
    footer(s, 8)


def slide_team(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    bg(s)
    kicker(s, "팀 운영")
    title(s, "역할은 이렇게 나누면 됩니다")

    roles = [
        ("A", "회원 · 상품", "가입, 로그인\n상품·매장·영화 등록\n목록과 검색"),
        ("B", "주문 · 예약", "장바구니 또는 신청\n중복·재고 검증\n상태 변경"),
        ("C", "관리자 · 내역", "마이페이지\n주문·예약 조회\n매출·정원 현황"),
    ]
    for i, (mark, name, body) in enumerate(roles):
        x = 0.48 + i * 4.18
        rect(s, x, 1.45, 4.0, 2.35, WHITE, line=LINE, radius=0.08)
        rect(s, x + 0.22, 1.68, 0.55, 0.55, NAVY, radius=0.2)
        add_text(s, x + 0.22, 1.68, 0.55, 0.55, mark, size=18, bold=True, color=WHITE, align="center", anchor="middle")
        add_text(s, x + 0.92, 1.72, 2.8, 0.5, name, size=20, bold=True, color=NAVY, anchor="middle")
        add_text(s, x + 0.28, 2.4, 3.5, 1.2, body, size=14, color=INK)

    add_text(s, 0.48, 4.0, 8, 0.4, "처음이라면 이 세 개 중 하나", size=18, bold=True, color=NAVY)
    picks = [
        ("01", "온라인 서점", "테이블 관계가 교과서와 같습니다.", SHOP),
        ("02", "카페 픽업", "상태가 짧아서 흐름이 보입니다.", SVC),
        ("06", "원데이 클래스", "정원만 막으면 예약이 끝납니다.", BOOK),
    ]
    for i, (num, name, why, color) in enumerate(picks):
        x = 0.48 + i * 4.18
        rect(s, x, 4.5, 4.0, 2.2, WHITE, line=LINE, radius=0.08)
        rect(s, x, 4.5, 0.1, 2.2, color)
        add_text(s, x + 0.3, 4.68, 3.4, 0.32, num, size=13, bold=True, color=color)
        add_text(s, x + 0.3, 5.0, 3.5, 0.45, name, size=22, bold=True, color=NAVY)
        add_text(s, x + 0.3, 5.55, 3.5, 0.85, why, size=15, color=INK)
    footer(s, 9)


def build():
    prs = Presentation()
    prs.slide_width = Inches(W)
    prs.slide_height = Inches(H)
    prs.core_properties.title = "B2C 팀 프로젝트 주제 10선"
    prs.core_properties.subject = "Spring Boot MyBatis JPA API"
    prs.core_properties.author = "팀 프로젝트 가이드"
    slide_cover(prs)
    slide_criteria(prs)
    slide_map(prs)
    slide_types(prs)
    slide_shop(prs)
    slide_book(prs)
    slide_service(prs)
    slide_stack(prs)
    slide_team(prs)
    prs.save(OUT)
    print(OUT)


if __name__ == "__main__":
    build()
