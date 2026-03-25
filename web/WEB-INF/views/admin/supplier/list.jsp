<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>

                <t:layout title="Supplier Management">
                    <jsp:attribute name="actions">
                        <t:link url="${pageContext.request.contextPath}/admin/supplier/create" color="primary"
                            variant="split" icon="plus-lg">
                            Create
                        </t:link>
                    </jsp:attribute>

                    <jsp:body>
                        <c:set var="columns" value='${["Index", "Code", "Name", "Email", "Phone", "Action"]}' />
                        <t:table columns="${columns}">
                            <jsp:attribute name="head">
                                <form hx-get="${pageContext.request.contextPath}/admin/supplier" hx-target="#wrapper"
                                    hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="input-group m-0">
                                    <input name="search" class="form-control"
                                        placeholder="Search by name, code, email, phone" value="${search}" />
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-search"></i>
                                    </button>
                                </form>
                            </jsp:attribute>

                            <jsp:attribute name="foot">
                                <t:pagination page="${page}" pages="${pages}" size="${size}" total="${total}"
                                    url="${pageContext.request.contextPath}/admin/supplier"
                                    include="[name='search'], [name='sort']" />
                            </jsp:attribute>

                            <jsp:body>
                                <c:forEach var="s" items="${suppliers}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1 + (page - 1) * size}</td>
                                        <td><strong>${s.code}</strong></td>
                                        <td>${s.name}</td>
                                        <td>${s.email}</td>
                                        <td>${s.phone}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/supplier/detail?id=${s.supplierId}"
                                                class="btn btn-sm btn-circle btn-outline-info me-1" title="View Detail">
                                                <i class="bi bi-eye fab"></i>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/supplier/update?id=${s.supplierId}"
                                                class="btn btn-sm btn-circle btn-outline-primary me-1" title="Update">
                                                <i class="bi bi-pencil fab"></i>
                                            </a>

                                            <button type="button" class="btn btn-sm btn-circle btn-outline-danger"
                                                data-bs-toggle="modal" data-bs-target="#deleteModal${s.supplierId}"
                                                title="Delete">
                                                <i class="bi bi-trash fab"></i>
                                            </button>

                                            <t:alert id="deleteModal${s.supplierId}">
                                                <jsp:attribute name="title"> Confirm Change </jsp:attribute>
                                                <jsp:attribute name="desciption">
                                                    Are you sure you want to change status of supplier
                                                    <strong>${s.name}</strong>? This action cannot be undone.
                                                </jsp:attribute>
                                                <jsp:attribute name="action">
                                                    <button type="button" class="btn btn-danger"
                                                        data-bs-dismiss="modal"
                                                        onclick="document.getElementById('deleteForm${s.supplierId}').submit()">
                                                        Change
                                                    </button>
                                                </jsp:attribute>
                                            </t:alert>
                                            <form id="deleteForm${s.supplierId}" method="POST"
                                                action="${pageContext.request.contextPath}/admin/supplier/delete"
                                                class="d-none">
                                                <input type="hidden" name="id" value="${s.supplierId}">
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </jsp:body>
                        </t:table>

                        <c:if test="${not empty emptySearchMessage}">
                            <span class="d-none js-empty-search-toast" data-empty-search-toast="${fn:escapeXml(emptySearchMessage)}" aria-hidden="true"></span>
                        </c:if>
                    </jsp:body>
                </t:layout>