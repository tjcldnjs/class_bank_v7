<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/view/layout/header.jsp"%>
<div class="container p-5">
    <div class="card">
        <div class="card-header"><b>익명 글쓰기 화면입니다</b></div>
        <div class="card-body">
            <form action="#">
                <div class="mb-3">
                    <input type="text" class="form-control" placeholder="Enter author" name="name">
                </div>
                <div class="mb-3">
                    <input type="text" class="form-control" placeholder="Enter title" name="title">
                </div>
                <div class="mb-3">
                    <textarea class="form-control" rows="5" name="content"></textarea>
                </div>
                <button type="submit" class="btn btn-primary form-control">글쓰기완료</button>
            </form>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>