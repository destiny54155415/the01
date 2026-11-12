
package com.example.shopping_project.service.addressservice;

import com.example.shopping_project.entity.addressentity.AddressEntity;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.addressrepository.AddressRepository;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    // 회원의 배송지 전체 조회
    @Transactional(readOnly = true)
    public List<AddressEntity> getAddressList(Long memberId) {

        MemberEntity member = findMember(memberId);

        return addressRepository
                .findAllByMemberOrderByIsDefaultDescIdDesc(member);
    }

    // 배송지 추가
    public AddressEntity addAddress(Long memberId, String addressName, String recipient,
                                    String phone, String zipcode, String address, String addressDetail,
                                    boolean makeDefault) {

        MemberEntity member = findMember(memberId);

        List<AddressEntity> addressList =
                addressRepository
                        .findAllByMemberOrderByIsDefaultDescIdDesc(member);

        // 첫 번째 배송지는 자동으로 기본 배송지
        boolean isDefault = makeDefault || addressList.isEmpty();

        if (isDefault) {
            addressList.forEach(a -> a.setDefault(false));
        }

        AddressEntity newAddress = new AddressEntity(
                member, addressName, recipient, phone, zipcode,
                address, addressDetail, isDefault);

        return addressRepository.save(newAddress);
    }

    // 배송지 수정
    public void updateAddress(
            Long memberId,
            Long addressId,
            String addressName,
            String recipient,
            String phone,
            String zipcode,
            String address,
            String addressDetail) {

        MemberEntity member = findMember(memberId);
        AddressEntity savedAddress = findAddress(addressId, member);

        savedAddress.update(
                addressName,
                recipient,
                phone,
                zipcode,
                address,
                addressDetail
        );
    }

    // 배송지 삭제
    public void deleteAddress(Long memberId, Long addressId) {

        MemberEntity member = findMember(memberId);
        AddressEntity address = findAddress(addressId, member);

        boolean wasDefault = address.isDefault();

        addressRepository.delete(address);

        // 기본 배송지를 삭제했다면 다른 배송지를 기본으로 설정
        if (wasDefault) {

            List<AddressEntity> remainingAddresses =
                    addressRepository
                            .findAllByMemberOrderByIsDefaultDescIdDesc(member);

            if (!remainingAddresses.isEmpty()) {
                remainingAddresses.get(0).setDefault(true);
            }
        }
    }

    // 기본 배송지 변경
    public void setDefaultAddress(Long memberId, Long addressId) {

        MemberEntity member = findMember(memberId);
        AddressEntity targetAddress = findAddress(addressId, member);

        List<AddressEntity> addressList =
                addressRepository
                        .findAllByMemberOrderByIsDefaultDescIdDesc(member);

        // 기존 기본 배송지 해제
        addressList.forEach(a -> a.setDefault(false));

        // 선택한 배송지를 기본으로 설정
        targetAddress.setDefault(true);
    }

    // 회원의 기본 배송지 조회
    @Transactional(readOnly = true)
    public AddressEntity getDefaultAddress(Long memberId) {

        MemberEntity member = findMember(memberId);

        return addressRepository
                .findByMemberAndIsDefaultTrue(member)
                .orElse(null);
    }

    // 회원 조회
    private MemberEntity findMember(Long memberId) {

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    // 회원 소유 배송지 조회
    private AddressEntity findAddress(
            Long addressId,
            MemberEntity member) {

        return addressRepository
                .findByIdAndMember(addressId, member)
                .orElseThrow(() ->
                        new IllegalArgumentException("배송지를 찾을 수 없습니다."));
    }
}
