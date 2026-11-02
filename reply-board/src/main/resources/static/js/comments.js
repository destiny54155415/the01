(function () {
    const app = document.getElementById("commentApp");
    if (!app) {
        return;
    }

    const postId = app.dataset.postId;
    const listEl = document.getElementById("commentList");
    const form = document.getElementById("commentForm");
    const writerInput = document.getElementById("commentWriter");
    const contentInput = document.getElementById("commentContent");
    const errorEl = document.getElementById("commentError");
    const countEls = document.querySelectorAll("[data-comment-count]");
    let order = "asc";

    writerInput.value = localStorage.getItem("boardWriter") || "foxman12";

    document.getElementById("sortAsc").addEventListener("click", function () {
        setOrder("asc");
    });
    document.getElementById("sortDesc").addEventListener("click", function () {
        setOrder("desc");
    });
    document.getElementById("likeButton").addEventListener("click", like);
    document.getElementById("copyUrl").addEventListener("click", copyUrl);
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        submitComment(null, writerInput.value, contentInput.value, form.querySelector("button"))
            .then(function (ok) {
                if (ok) {
                    contentInput.value = "";
                }
            });
    });
    document.addEventListener("click", function (event) {
        if (!event.target.closest(".menu")) {
            closeMenus();
        }
    });

    load();

    function setOrder(next) {
        order = next;
        document.getElementById("sortAsc").classList.toggle("active", order === "asc");
        document.getElementById("sortDesc").classList.toggle("active", order === "desc");
        load();
    }

    function load() {
        return fetch("/api/posts/" + postId + "/comments?order=" + order)
            .then(function (response) {
                return response.json();
            })
            .then(render)
            .catch(function () {
                showError("댓글을 불러오지 못했습니다.");
            });
    }

    function render(comments) {
        showError("");
        listEl.replaceChildren();
        const total = countAll(comments);
        countEls.forEach(function (el) {
            el.textContent = String(total);
        });
        if (comments.length === 0) {
            const empty = document.createElement("p");
            empty.className = "empty";
            empty.textContent = "첫 댓글을 남겨보세요.";
            listEl.append(empty);
            return;
        }
        comments.forEach(function (comment) {
            listEl.append(node(comment, 0));
        });
    }

    function countAll(comments) {
        return comments.reduce(function (sum, comment) {
            return sum + 1 + countAll(comment.replies || []);
        }, 0);
    }

    function node(comment, depth) {
        const article = document.createElement("article");
        article.className = "comment";
        article.style.marginLeft = (depth * 28) + "px";

        const head = document.createElement("div");
        head.className = "comment-head";
        const who = document.createElement("div");
        who.className = "who";
        const avatar = document.createElement("span");
        avatar.className = "avatar";
        avatar.textContent = comment.writer.slice(0, 1);
        const meta = document.createElement("div");
        const name = document.createElement("strong");
        name.textContent = comment.writer;
        const time = document.createElement("time");
        time.textContent = comment.createdAt;
        meta.append(name, time);
        who.append(avatar, meta);
        head.append(who, menu(comment, article));

        const body = document.createElement("p");
        body.className = "comment-body";
        body.textContent = comment.content;

        const actions = document.createElement("div");
        actions.className = "comment-actions";
        const replyBtn = document.createElement("button");
        replyBtn.type = "button";
        replyBtn.className = "text-btn";
        replyBtn.textContent = "답글";
        replyBtn.addEventListener("click", function () {
            toggleReply(article, comment.id);
        });
        actions.append(replyBtn);

        article.append(head, body, actions);
        (comment.replies || []).forEach(function (child) {
            article.append(node(child, depth + 1));
        });
        return article;
    }

    function menu(comment, article) {
        const wrap = document.createElement("div");
        wrap.className = "menu";
        const opener = document.createElement("button");
        opener.type = "button";
        opener.className = "text-btn";
        opener.textContent = "⋯";
        opener.setAttribute("aria-label", "댓글 메뉴");
        const popup = document.createElement("div");
        popup.className = "popup";
        popup.hidden = true;

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.textContent = "수정";
        editBtn.addEventListener("click", function () {
            popup.hidden = true;
            startEdit(article, comment);
        });
        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.textContent = "삭제";
        deleteBtn.addEventListener("click", function () {
            popup.hidden = true;
            remove(comment.id);
        });
        popup.append(editBtn, deleteBtn);
        opener.addEventListener("click", function (event) {
            event.stopPropagation();
            const open = popup.hidden;
            closeMenus();
            popup.hidden = !open;
        });
        wrap.append(opener, popup);
        return wrap;
    }

    function closeMenus() {
        document.querySelectorAll(".popup").forEach(function (popup) {
            popup.hidden = true;
        });
    }

    function toggleReply(article, parentId) {
        const existing = article.querySelector(":scope > .reply-form");
        if (existing) {
            existing.remove();
            return;
        }
        const box = document.createElement("form");
        box.className = "reply-form";
        const writer = document.createElement("input");
        writer.type = "text";
        writer.maxLength = 30;
        writer.required = true;
        writer.value = writerInput.value;
        writer.placeholder = "작성자";
        const text = document.createElement("textarea");
        text.required = true;
        text.maxLength = 1000;
        text.rows = 2;
        text.placeholder = "답글을 남겨보세요";
        const bar = document.createElement("div");
        bar.className = "composer-bar";
        const submit = document.createElement("button");
        submit.type = "submit";
        submit.className = "button";
        submit.textContent = "등록";
        bar.append(submit);
        box.append(writer, text, bar);
        box.addEventListener("submit", function (event) {
            event.preventDefault();
            submitComment(parentId, writer.value, text.value, submit).then(function (ok) {
                if (ok) {
                    box.remove();
                }
            });
        });
        const actions = article.querySelector(":scope > .comment-actions");
        actions.insertAdjacentElement("afterend", box);
        text.focus();
    }

    function startEdit(article, comment) {
        const body = article.querySelector(":scope > .comment-body");
        if (!body || article.querySelector(":scope > .edit-form")) {
            return;
        }
        const original = comment.content;
        body.hidden = true;
        const box = document.createElement("form");
        box.className = "edit-form";
        const text = document.createElement("textarea");
        text.required = true;
        text.maxLength = 1000;
        text.rows = 3;
        text.value = original;
        const bar = document.createElement("div");
        bar.className = "composer-bar";
        const cancel = document.createElement("button");
        cancel.type = "button";
        cancel.className = "text-btn";
        cancel.textContent = "취소";
        const save = document.createElement("button");
        save.type = "submit";
        save.className = "button";
        save.textContent = "저장";
        bar.append(cancel, save);
        box.append(text, bar);
        cancel.addEventListener("click", function () {
            box.remove();
            body.hidden = false;
        });
        box.addEventListener("submit", function (event) {
            event.preventDefault();
            save.disabled = true;
            fetch("/api/comments/" + comment.id, {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({content: text.value})
            }).then(function (response) {
                return response.json().then(function (data) {
                    return {ok: response.ok, data: data};
                });
            }).then(function (result) {
                if (!result.ok) {
                    showError(result.data.message || "수정하지 못했습니다.");
                    save.disabled = false;
                    return;
                }
                load();
            }).catch(function () {
                showError("수정하지 못했습니다.");
                save.disabled = false;
            });
        });
        body.insertAdjacentElement("afterend", box);
        text.focus();
    }

    function submitComment(parentId, writer, content, button) {
        const name = writer.trim();
        const text = content.trim();
        if (!name || !text) {
            showError("작성자와 댓글을 입력하세요.");
            return Promise.resolve(false);
        }
        button.disabled = true;
        localStorage.setItem("boardWriter", name);
        writerInput.value = name;
        return fetch("/api/posts/" + postId + "/comments", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({writer: name, content: text, parentId: parentId})
        }).then(function (response) {
            return response.json().then(function (data) {
                return {ok: response.ok, data: data};
            });
        }).then(function (result) {
            button.disabled = false;
            if (!result.ok) {
                showError(result.data.message || "등록하지 못했습니다.");
                return false;
            }
            showError("");
            return load().then(function () {
                return true;
            });
        }).catch(function () {
            button.disabled = false;
            showError("등록하지 못했습니다.");
            return false;
        });
    }

    function remove(id) {
        if (!confirm("댓글을 삭제할까요?")) {
            return;
        }
        fetch("/api/comments/" + id, {method: "DELETE"})
            .then(function (response) {
                if (!response.ok && response.status !== 204) {
                    showError("삭제하지 못했습니다.");
                    return;
                }
                load();
            })
            .catch(function () {
                showError("삭제하지 못했습니다.");
            });
    }

    function like() {
        fetch("/api/posts/" + postId + "/like", {method: "POST"})
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                document.getElementById("likeCount").textContent = data.likeCount;
            });
    }

    function copyUrl() {
        const button = document.getElementById("copyUrl");
        navigator.clipboard.writeText(location.href).then(function () {
            button.textContent = "복사됨";
            setTimeout(function () {
                button.textContent = "URL 복사";
            }, 1200);
        });
    }

    function showError(message) {
        errorEl.hidden = !message;
        errorEl.textContent = message;
    }
})();
