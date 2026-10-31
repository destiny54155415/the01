package com.team.catalog.web;

import com.team.catalog.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm {

    private Long id;
    private Long parentId;

    @NotBlank(message = "카테고리 이름을 입력해 주세요.")
    @Size(max = 40, message = "카테고리 이름은 40자 이하입니다.")
    private String name;

    private int sortOrder;
    private boolean visible = true;

    public static CategoryForm from(Category category) {
        CategoryForm form = new CategoryForm();
        form.id = category.getId();
        form.parentId = category.getParent() == null ? null : category.getParent().getId();
        form.name = category.getName();
        form.sortOrder = category.getSortOrder();
        form.visible = category.isVisible();
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
