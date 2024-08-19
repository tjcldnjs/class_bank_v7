<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/view/layout/header.jsp"%>
    <div class="container p-5">
        <h2>게시글 목록</h2>
        <table class="table table-striped" border="1">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>제목</th>
                    <th>내용</th>
                    <th>작성자</th>
                    <th></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="blog" items="${blogList}">
                    <tr>
                        <td>${blog.id}</td>
                        <td>${blog.title}</td>
                        <td>${blog.content}</td>
                        <td>${blog.name}</td>
                        <td>
                        <div class="d-flex">
                            <form action="/board/delete" method="post">
                                <input type="hidden" name="id" value="${blog.id}">
                                 <button class="btn btn-danger">삭제</button>
                            </form>
                            <form action="/board/updateForm" method="get">
                                <input type="hidden" name="id" value="${blog.id}">
                                <button class="btn btn-warning">수정</button>
                            </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <br>

        <!-- Pagination -->
        <div class="pagination-container">
            <ul class="pagination">
                <li class="page-item <c:if test='${currentPage == 1}'>disabled</c:if>">
                    <a class="page-link" href="?page=${currentPage - 1}&size=${size}">Previous</a>
                </li>

                <c:forEach begin="1" end="${totalPages}" var="page">
                    <li class="page-item <c:if test='${page == currentPage}'>active</c:if>">
                        <a class="page-link" href="?page=${page}&size=${size}">${page}</a>
                    </li>
                </c:forEach>

                <li class="page-item <c:if test='${currentPage == totalPages}'>disabled</c:if>">
                    <a class="page-link" href="?page=${currentPage + 1}&size=${size}">Next</a>
                </li>
            </ul>
        </div>
    </div>
</body>
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>