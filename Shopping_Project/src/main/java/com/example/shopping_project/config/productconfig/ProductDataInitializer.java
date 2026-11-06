package com.example.shopping_project.config.productconfig;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.productrepository.CategoryRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 펫샵에서 잘 팔리는 SKU 위주로 카탈로그를 맞춘다.
 * description 첫 줄 = 목록/요약, 이후 = 상세 페이지 본문.
 */
@Component
@Profile("!test")
public class ProductDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductDataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    private static final List<String> CATEGORY_NAMES = List.of(
            "사료",
            "간식",
            "장난감",
            "산책용품",
            "미용·목욕",
            "하우스·생활용품",
            "의류·패션",
            "위생·배변용품"
    );

    @Override
    @Transactional
    public void run(String... args) {
        ensureCategories();
        syncBestsellers();
    }

    private void ensureCategories() {
        for (int i = 0; i < CATEGORY_NAMES.size(); i++) {
            String name = CATEGORY_NAMES.get(i);
            int order = i + 1;
            categoryRepository.findByCategoryName(name).ifPresentOrElse(existing -> {
                existing.setDisplayOrder(order);
                existing.setIsActive(true);
            }, () -> categoryRepository.save(new CategoryEntity(name, order)));
        }
    }

    private CategoryEntity category(String name) {
        return categoryRepository.findByCategoryName(name)
                .orElseThrow(() -> new IllegalStateException("카테고리 없음: " + name));
    }

    private void syncBestsellers() {
        CategoryEntity food = category("사료");
        CategoryEntity snack = category("간식");
        CategoryEntity toy = category("장난감");
        CategoryEntity walk = category("산책용품");
        CategoryEntity bath = category("미용·목욕");
        CategoryEntity house = category("하우스·생활용품");
        CategoryEntity fashion = category("의류·패션");
        CategoryEntity hygiene = category("위생·배변용품");

        upsert(food, "실내묘 헤어볼 케어 사료 2kg",
                detail("국내 펫샵 스테디셀러. 헤어볼·실내묘 전용 건식 사료입니다.",
                        "실내에서 생활하는 고양이를 위해 헤어볼 케어 성분을 더한 건식 사료입니다. 적당한 알갱이 크기로 기호성이 좋아요.",
                        "실내묘, 털빠짐이 많은 고양이, 하루 두 끼 규칙적으로 급여하는 가정",
                        "용량 2kg / 대상 고양이 / 건식 사료",
                        "하루 권장량을 2회로 나눠 급여하세요. 신선한 물과 함께 주세요.",
                        "급격한 사료 변경은 피하고 7일간 기존 사료와 섞어 전환하세요."),
                "32900", 85, 1820, PetType.CAT, "/images/food-cat.jpg");
        upsert(food, "소형견 전연령 건식사료 2kg",
                detail("소형견 보호자 구매가 많은 기본 사료입니다.",
                        "소형견의 작은 구강에 맞춘 키블 사이즈로, 전연령이 먹기 편한 기본 건식 사료입니다.",
                        "소형견, 처음 건식을 고르는 보호자, 가성비 좋은 주식 찾는 분",
                        "용량 2kg / 대상 강아지 / 전연령",
                        "체중·활동량에 맞춰 하루 급여량을 조절해 주세요.",
                        "개봉 후 밀봉 보관하고 한 달 안에 급여하는 것을 권장합니다."),
                "29900", 70, 1410, PetType.DOG, "/images/food-dog.jpg");
        upsert(food, "고양이 주식 파우치 12개입",
                detail("습식 주식으로 자주 재구매되는 파우치 세트입니다.",
                        "촉촉한 습식 파우치로 수분 보충과 기호성을 동시에 챙길 수 있는 주식 세트입니다.",
                        "물을 잘 안 마시는 고양이, 건식과 함께 급여하는 가정",
                        "12개입 / 고양이 주식 파우치",
                        "개봉 후 바로 급여하고, 남긴 사료는 냉장 보관 후 당일 사용하세요.",
                        "알레르기 반응이 있으면 급여를 중단하고 상담하세요."),
                "18900", 95, 1560, PetType.CAT, "/images/food-pouch.jpg");
        upsert(food, "고양이 주식캔 6개입",
                detail("기호성 좋은 습식 캔. 파우치와 함께 많이 담기는 기본 주식입니다.",
                        "부드럽고 촉촉한 캔 타입으로, 파우치와 번갈아 주면 질리지 않게 급여할 수 있어요.",
                        "습식을 좋아하는 고양이, 선물·첫 구매용 세트를 찾는 분",
                        "6개입 / 고양이 주식캔",
                        "상온 보관, 개봉 후 당일 급여를 권장합니다.",
                        "캔 가장자리에 주의하고 반려동물 손이 닿지 않게 보관하세요."),
                "15900", 100, 1320, PetType.CAT, "/images/food-can.jpg");
        upsert(food, "퍼피·키튼 스타터 사료 1.5kg",
                detail("성장기 전용 소프트 키블. 귀여운 패키지로 선물용 구매도 많은 사료입니다.",
                        "퍼피·키튼 성장에 맞춘 부드러운 알갱이로, 처음 고형식을 시작하는 아이에게 적합합니다.",
                        "이유식 이후 첫 사료, 선물용 스타터 세트를 찾는 분",
                        "용량 1.5kg / 퍼피·키튼 / 공용",
                        "소량부터 시작해 배변 상태를 보며 양을 늘려 주세요.",
                        "성견·성묘 전환기에는 전용 사료로 천천히 바꿔 주세요."),
                "27900", 65, 980, PetType.COMMON, "/images/food-starter.jpg");

        upsert(snack, "짜먹는 츄르 참치 20개입",
                detail("고양이 간식 부동의 1위. 기호성 좋은 참치 츄르입니다.",
                        "부드럽고 촉촉한 참치맛 츄르로, 간식·약 급여·스킨십용으로 폭넓게 쓰입니다.",
                        "간식을 좋아하는 고양이, 약 먹이기용 간식이 필요한 분",
                        "20개입 / 고양이 간식",
                        "하루 1~2개를 권장하며, 과다 급여는 피하세요.",
                        "개봉 후 바로 급여하고, 보관 중 파손·변질 여부를 확인하세요."),
                "12900", 200, 4200, PetType.CAT, "/images/churu-tuna.jpg");
        upsert(snack, "짜먹는 츄르 연어 20개입",
                detail("참치와 함께 가장 많이 팔리는 연어맛 츄르입니다.",
                        "고소한 연어맛으로 참치와 번갈아 주면 질리지 않는 인기 간식입니다. 부드러운 텍스처로 전연령 급여에 좋아요.",
                        "연어를 좋아하는 고양이, 참치 츄르와 세트로 구매하는 분",
                        "20개입 / 고양이 간식 / 연어맛",
                        "실온 보관 후 짜서 급여하세요. 훈련·칭찬용으로도 적합합니다.",
                        "알레르기 병력이 있는 경우 성분 확인 후 급여하세요."),
                "12900", 180, 3650, PetType.CAT, "/images/churu-salmon.jpg");
        upsert(snack, "강아지 져키 간식 200g",
                detail("산책·훈련 보상용으로 재구매율이 높은 져키입니다.",
                        "씹는 재미가 있는 져키 타입으로, 산책 중 보상과 실내 훈련에 자주 사용됩니다.",
                        "소형·중형견, 산책 훈련 중인 보호자",
                        "200g / 강아지 간식",
                        "한 번에 소량씩 급여하고, 물과 함께 주세요.",
                        "질식 위험이 있으니 큰 덩어리는 잘라 급여하세요."),
                "9900", 110, 980, PetType.DOG, "/images/jerky.jpg");
        upsert(snack, "동결건조 트릿 60g",
                detail("토핑·훈련용으로 인기인 동결건조 간식입니다.",
                        "바삭한 동결건조 트릿으로 사료 토핑이나 훈련 보상으로 활용도가 높습니다.",
                        "강아지·고양이 공용 간식, 토핑을 좋아하는 가정",
                        "60g / 공용 동결건조 트릿",
                        "밀봉 용기를 권장하며, 습기를 피해 보관하세요.",
                        "처음에는 소량만 급여해 적응을 확인하세요."),
                "15900", 75, 1120, PetType.COMMON, "/images/treat.jpg");

        upsert(toy, "고양이 낚싯대 2종 세트",
                detail("사냥 본능 자극용으로 꾸준히 팔리는 기본 장난감입니다.",
                        "깃털·끈 타입이 포함된 낚싯대 세트로, 짧은 플레이로도 활동량을 채우기 좋아요.",
                        "실내묘, 사냥 놀이를 좋아하는 고양이",
                        "2종 세트 / 고양이 장난감",
                        "하루 10~15분씩 짧게 놀아주고, 사용 후 보관하세요.",
                        "줄이 엉키거나 손상되면 즉시 교체하세요."),
                "8900", 120, 1340, PetType.CAT, "/images/toy-wand.jpg");
        upsert(toy, "캣닢 쿠션 토이",
                detail("캣닢 장난감 중 재고 회전이 빠른 인기 상품입니다.",
                        "부드러운 쿠션형 캣닢 토이로, 혼자 놀기·스트레스 완화에 도움을 줍니다.",
                        "캣닢에 반응하는 고양이, 혼자 두는 시간이 긴 가정",
                        "1개 / 고양이 캣닢 토이",
                        "처음에는 짧게 제공하고 반응을 살펴 주세요.",
                        "찢어지면 속재료를 삼키지 않도록 바로 치우세요."),
                "5900", 140, 1210, PetType.CAT, "/images/toy-catnip.jpg");
        upsert(toy, "강아지 공 장난감 3개입",
                detail("실내 놀이용으로 많이 찾는 저가 묶음 상품입니다.",
                        "튕기고 물고 놀기 좋은 공 3개 세트로, 실내 활동량을 가볍게 채워 줍니다.",
                        "소형견, 실내 놀이가 필요한 가정",
                        "3개입 / 강아지 공 장난감",
                        "미끄러운 바닥에서는 미끄럼 주의하며 놀아 주세요.",
                        "파손된 조각은 삼킴 위험이 있으니 교체하세요."),
                "7900", 90, 760, PetType.DOG, "/images/toy-ball.jpg");

        upsert(walk, "쿠션 하네스 M",
                detail("산책 필수템. 목줄 대신 많이 선택되는 하네스입니다.",
                        "가슴 쿠션이 있어 당김이 있어도 편안한 착용감을 주는 하네스입니다.",
                        "목줄 대신 하네스를 선호하는 소형·중형견 보호자",
                        "사이즈 M / 강아지 하네스",
                        "두 손가락이 들어갈 정도로 조절 후 산책하세요.",
                        "첫 착용 시 실내에서 적응 시간을 주세요."),
                "24900", 55, 890, PetType.DOG, "/images/harness.jpg");
        upsert(walk, "자동 리드줄 5m",
                detail("중형견 보호자에게 판매량이 높은 자동 리드줄입니다.",
                        "최대 5m까지 늘어나는 자동 리드줄로, 공원 산책 시 활동 범위를 조절하기 좋아요.",
                        "중형견, 넓은 공간 산책을 자주 하는 분",
                        "길이 5m / 강아지 자동 리드줄",
                        "사람·자전거가 많은 곳에서는 짧게 고정해 사용하세요.",
                        "줄이 꼬이거나 마모되면 교체하세요."),
                "21900", 48, 720, PetType.DOG, "/images/leash.jpg");
        upsert(walk, "고양이 산책 하네스 세트",
                detail("실외 적응·병원 이동용으로 찾는 고양이 전용 하네스+리드 세트입니다.",
                        "고양이 체형에 맞춘 하네스와 리드 구성으로, 병원 이동·발코니·짧은 산책에 사용합니다.",
                        "바깥 적응을 시작하는 고양이, 이동 시 안정이 필요한 분",
                        "하네스+리드 세트 / 고양이 전용",
                        "실내에서 충분히 착용감을 익힌 뒤 외출하세요.",
                        "빠져나올 수 있으니 처음에는 안전한 공간에서만 사용하세요."),
                "19900", 42, 560, PetType.CAT, "/images/cat-harness.jpg");

        upsert(bath, "저자극 펫샴푸 500ml",
                detail("목욕 시즌·정기 구매가 많은 저자극 샴푸입니다.",
                        "피부 자극을 줄인 저자극 포뮬러로 강아지·고양이 공용 목욕에 사용할 수 있습니다.",
                        "정기 목욕, 민감 피부가 신경 쓰이는 반려동물",
                        "500ml / 공용 펫샴푸",
                        "미지근한 물로 적신 뒤 거품을 내어 마사지하듯 씻기고 충분히 헹구세요.",
                        "눈에 들어가지 않게 주의하고, 이상 시 사용을 중단하세요."),
                "16900", 60, 640, PetType.COMMON, "/images/shampoo.jpg");
        upsert(bath, "털제거 브러시",
                detail("털갈이 시즌마다 판매가 크게 오르는 브러시입니다.",
                        "죽은 털을 부드럽게 걷어내는 브러시로, 털갈이 시기 관리에 특히 유용합니다.",
                        "장모·단모 모두, 털갈이 시즌 관리가 필요한 가정",
                        "1개 / 공용 브러시",
                        "결 방향으로 천천히 빗질하고, 너무 세게 누르지 마세요.",
                        "피부 질환이 있으면 사용 전 상담을 권장합니다."),
                "12900", 80, 880, PetType.COMMON, "/images/brush.jpg");
        upsert(bath, "펫 목욕 가운 타월",
                detail("목욕 후 물기 제거용으로 함께 담기는 극세사 가운 타월입니다.",
                        "후드형 극세사 가운으로 목욕 후 물기를 빠르게 흡수해 줍니다. 귀여운 디자인으로 사진 찍기에도 좋아요.",
                        "목욕 후 관리, 드라이 전 물기 제거가 필요한 분",
                        "1개 / 공용 목욕 가운",
                        "목욕 직후 착용시켜 물기를 닦아 주세요.",
                        "세탁 후 완전히 말려 사용하세요."),
                "14900", 70, 510, PetType.COMMON, "/images/bath-towel.jpg");

        upsert(house, "고양이 스크래쳐 하우스",
                detail("스크래쳐+숨숨집 겸용으로 객단가가 높은 인기 상품입니다.",
                        "발톱 갈이와 숨숨집을 한 번에 해결하는 스크래쳐 하우스입니다. 거실 인테리어에도 잘 어울려요.",
                        "스크래칭이 필요한 고양이, 숨숨 공간을 좋아하는 아이",
                        "1개 / 고양이 스크래쳐 하우스",
                        "평평한 바닥에 두고, 캣닢을 조금 뿌리면 적응이 빨라집니다.",
                        "심하게 해지면 교체해 주세요."),
                "39900", 35, 970, PetType.CAT, "/images/house.jpg");
        upsert(house, "극세사 펫쿠션 L",
                detail("강아지·고양이 공용으로 회전율이 좋은 방석입니다.",
                        "폭신한 극세사 쿠션으로 휴식 공간을 만들어 줍니다. 세탁이 쉬운 커버 타입입니다.",
                        "공용 휴식처, 대형 방석이 필요한 가정",
                        "사이즈 L / 공용 펫쿠션",
                        "통풍이 되는 곳에 두고, 커버는 분리 세탁하세요.",
                        "화재 위험이 있는 전열기구 근처에는 두지 마세요."),
                "24900", 50, 830, PetType.COMMON, "/images/cushion.jpg");

        upsert(fashion, "방수 올인원 레인코트",
                detail("비 오는 날 수요가 몰리는 강아지 레인코트입니다.",
                        "가벼운 방수 소재의 올인원 레인코트로, 비 오는 날 산책 시 몸을 덜 젖게 해줍니다.",
                        "비 오는 날 산책이 잦은 소형·중형견",
                        "1개 / 강아지 레인코트",
                        "착용 후 배변 끈을 조절하고, 산책 후 물기를 닦아 주세요.",
                        "세탁 시 울긋지 않게 찬물 손세탁을 권장합니다."),
                "27900", 28, 310, PetType.DOG, "/images/raincoat.jpg");
        upsert(fashion, "후드 맨투맨",
                detail("환절기·실내복으로 많이 찾는 강아지 후드 맨투맨입니다.",
                        "부드러운 맨투맨 원단의 후드 탑으로, 환절기 체온 유지와 실내복으로 인기입니다.",
                        "환절기 옷이 필요한 소형견, 사진 찍기 좋은 옷을 찾는 분",
                        "1개 / 강아지 후드 맨투맨",
                        "머리부터 천천히 씌워 착용하세요.",
                        "장시간 혼자 둘 때는 벗겨 두는 것이 안전합니다."),
                "22900", 36, 420, PetType.DOG, "/images/hoodie.jpg");
        upsert(fashion, "니트 목도리 케이프",
                detail("사진·외출용으로 인기인 공용 니트 케이프입니다.",
                        "포근한 니트 케이프로 목과 어깨를 가볍게 감싸 줍니다. 산책·기념일 사진용으로 좋아요.",
                        "강아지·고양이 공용 포인트 아이템을 찾는 분",
                        "1개 / 공용 니트 케이프",
                        "느슨하게 둘러 답답하지 않게 착용하세요.",
                        "실밥이 나오면 잘라 내고, 물림·뜯김 시 사용을 중단하세요."),
                "15900", 45, 380, PetType.COMMON, "/images/boots.jpg");

        upsert(hygiene, "먼지 적은 두부모래 7L",
                detail("고양이 모래 카테고리 최다 판매. 응고력·저분진 타입입니다.",
                        "먼지가 적고 응고력이 좋은 두부모래로, 화장실 관리가 한결 수월합니다.",
                        "먼지 적은 모래를 찾는 집사, 응고형 모래 선호",
                        "7L / 고양이 두부모래",
                        "모래상자 높이 5~7cm를 유지하고 매일 응고 덩어리를 치우세요.",
                        "변기에 대량 버리지 말고 쓰레기 배출 규정을 지키세요."),
                "11900", 220, 5100, PetType.CAT, "/images/tofu-sand.jpg");
        upsert(hygiene, "벤토나이트 모래 10kg",
                detail("대용량으로 가성비 구매가 많은 벤토나이트 모래입니다.",
                        "응고력이 뛰어난 벤토나이트 대용량 팩으로, 여러 마리를 키우는 가정에 적합합니다.",
                        "대용량·가성비 모래, 다묘 가정",
                        "10kg / 고양이 벤토나이트",
                        "바닥이 보이지 않을 정도로 넉넉히 깔아 주세요.",
                        "분진이 날릴 수 있으니 환기하며 교체하세요."),
                "14900", 160, 3900, PetType.CAT, "/images/bentonite.jpg");
        upsert(hygiene, "배변패드 50매",
                detail("소형견 가정에서 정기 구매하는 기본 배변패드입니다.",
                        "흡수력 좋은 배변패드 50매 구성으로, 실내 배변 훈련과 일상 관리에 사용합니다.",
                        "소형견, 실내 배변 패드 사용 가정",
                        "50매 / 강아지 배변패드",
                        "같은 자리에 깔아 배변 위치를 학습시켜 주세요.",
                        "오염된 패드는 바로 교체해 위생을 유지하세요."),
                "15900", 100, 1180, PetType.DOG, "/images/pad.jpg");
        upsert(hygiene, "모래삽 + 보관통 세트",
                detail("모래 구매 시 함께 담기는 교차판매 인기 세트입니다.",
                        "모래 정리용 삽과 보관통이 세트로, 화장실 관리를 깔끔하게 도와줍니다.",
                        "두부모래·벤토나이트 구매자, 화장실 용품 세트를 찾는 분",
                        "모래삽 1 + 보관통 1 / 고양이 화장실 용품",
                        "응고 덩어리를 삽으로 걸러 보관통에 모아 주세요.",
                        "세척 후 물기를 말려 보관하세요."),
                "7900", 95, 1450, PetType.CAT, "/images/scoop.jpg");
    }

    private static String detail(String summary, String intro, String recommend,
                                 String spec, String usage, String caution) {
        return summary
                + "\n\n【상품 소개】\n" + intro
                + "\n\n【이런 분께 추천】\n" + recommend
                + "\n\n【구성 / 용량】\n" + spec
                + "\n\n【사용 방법】\n" + usage
                + "\n\n【주의사항】\n" + caution;
    }

    private void upsert(CategoryEntity category, String name, String description, String price,
                        int stock, int salesCount, PetType petType, String imageUrl) {
        String[] gallery = galleryFor(category.getCategoryName(), name, imageUrl);
        productRepository.findByProductName(name).ifPresentOrElse(existing -> {
            existing.setCategory(category);
            existing.setDescription(description);
            existing.setPrice(new BigDecimal(price));
            existing.setStock(stock);
            existing.setSalesCount(salesCount);
            existing.setPetType(petType);
            existing.setImageUrl(gallery[0]);
            existing.setImageUrl2(gallery[1]);
            existing.setImageUrl3(gallery[2]);
            existing.setIsActive(true);
        }, () -> {
            ProductEntity created = new ProductEntity(
                    category, name, description, new BigDecimal(price), stock, salesCount, petType, gallery[0]);
            created.setImageUrl2(gallery[1]);
            created.setImageUrl3(gallery[2]);
            productRepository.save(created);
        });
    }

    /** 대표컷 + 박스 형태 + 제품 분리컷 */
    private String[] galleryFor(String categoryName, String productName, String mainImage) {
        if (productName.contains("두부모래")) {
            return new String[]{
                    mainImage,
                    "/images/gallery/tofu-sand-box.jpg",
                    "/images/gallery/tofu-sand-open.jpg"
            };
        }
        String box;
        String open;
        switch (categoryName) {
            case "사료" -> {
                box = "/images/gallery/food-box.jpg";
                open = "/images/gallery/food-open.jpg";
            }
            case "간식" -> {
                box = "/images/gallery/snack-box.jpg";
                open = "/images/gallery/snack-open.jpg";
            }
            case "장난감" -> {
                box = "/images/gallery/toy-box.jpg";
                open = "/images/gallery/toy-open.jpg";
            }
            case "산책용품" -> {
                box = "/images/gallery/walk-box.jpg";
                open = "/images/gallery/walk-open.jpg";
            }
            case "미용·목욕" -> {
                box = "/images/gallery/bath-box.jpg";
                open = "/images/gallery/bath-open.jpg";
            }
            case "하우스·생활용품" -> {
                box = "/images/gallery/house-box.jpg";
                open = "/images/gallery/house-open.jpg";
            }
            case "의류·패션" -> {
                box = "/images/gallery/fashion-box.jpg";
                open = "/images/gallery/fashion-open.jpg";
            }
            case "위생·배변용품" -> {
                box = "/images/gallery/hygiene-box.jpg";
                open = "/images/gallery/hygiene-open.jpg";
            }
            default -> {
                box = "/images/gallery/gallery-box.jpg";
                open = "/images/gallery/gallery-open.jpg";
            }
        }
        return new String[]{mainImage, box, open};
    }
}
