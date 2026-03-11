<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>

                <t:layout title="Warehouse Management">
                    <jsp:attribute name="actions">
                        <t:link url="${pageContext.request.contextPath}/admin/warehouse/create" color="primary"
                            variant="split" icon="plus-lg">
                            Create
                        </t:link>
                    </jsp:attribute>

                    <jsp:body>
                        <c:set var="columns"
                            value='${["Index", "Code", "Name", "Status", "Action"]}' />
                        <t:table columns="${columns}">
                            <jsp:attribute name="head">
                                <form hx-get="${pageContext.request.contextPath}/admin/warehouse" hx-target="#wrapper"
                                    hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="input-group m-0">
                                    <input name="search" class="form-control"
                                        placeholder="Search by name, code" value="${search}" />
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-search"></i>
                                    </button>
                                </form>
                            </jsp:attribute>

                            <jsp:attribute name="foot">
                                <t:pagination page="${page}" pages="${pages}" size="${size}" total="${total}"
                                    url="${pageContext.request.contextPath}/admin/warehouse"
                                    include="[name='search'], [name='sort']" />
                            </jsp:attribute>

                            <jsp:body>
                                <c:forEach var="w" items="${warehouses}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1 + (page - 1) * size}</td>
                                        <td><strong>${w.code}</strong></td>
                                        <td>${w.name}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${w.status == 'ACTIVE'}">
                                                    <span class="badge bg-success">Active</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${w.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/warehouse/detail?id=${w.warehouseId}"
                                                class="btn btn-sm btn-circle btn-outline-info me-1" title="View Detail">
                                                <i class="bi bi-eye fab"></i>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/warehouse/update?id=${w.warehouseId}"
                                                class="btn btn-sm btn-circle btn-outline-primary me-1" title="Update">
                                                <i class="bi bi-pencil fab"></i>
                                            </a>

                                            <button type="button" class="btn btn-sm btn-circle btn-outline-danger"
                                                data-bs-toggle="modal" data-bs-target="#deleteModal${w.warehouseId}"
                                                title="Delete">
                                                <i class="bi bi-trash fab"></i>
                                            </button>

                                            <t:alert id="deleteModal${w.warehouseId}">
                                                <jsp:attribute name="title"> Confirm Delete </jsp:attribute>
                                                <jsp:attribute name="desciption">
                                                    Are you sure you want to delete warehouse
                                                    <strong>${w.name}</strong>? This action cannot be undone.
                                                </jsp:attribute>
                                                <jsp:attribute name="action">
                                                    <form method="POST" action="${pageContext.request.contextPath}/admin/warehouse/delete" style="display:inline;">
                                                        <input type="hidden" name="id" value="${w.warehouseId}">
                                                        <button type="submit" class="btn btn-danger" data-bs-dismiss="modal">
                                                            Delete
                                                        </button>
                                                    </form>
                                                </jsp:attribute>
                                            </t:alert>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </jsp:body>
                        </t:table>
                    </jsp:body>
                </t:layout>