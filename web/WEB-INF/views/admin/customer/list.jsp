<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>

                <t:layout title="Customer Management">
                    <jsp:attribute name="actions">
                        <t:link url="${pageContext.request.contextPath}/admin/customer/create" color="primary"
                            variant="split" icon="plus-lg">
                            Create
                        </t:link>
                    </jsp:attribute>

                    <jsp:body>
                        <c:set var="columns" value='${["Index", "Code", "Name", "Email", "Phone", "Status", "Action"]}' />
                        <t:table columns="${columns}">
                            <jsp:attribute name="head">
                                <form hx-get="${pageContext.request.contextPath}/admin/customer" hx-target="#wrapper"
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
                                    url="${pageContext.request.contextPath}/admin/customer"
                                    include="[name='search'], [name='sort']" />
                            </jsp:attribute>

                            <jsp:body>
                                <c:forEach var="c" items="${customers}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1 + (page - 1) * size}</td>
                                        <td><strong>${c.code}</strong></td>
                                        <td>${c.name}</td>
                                        <td>${c.email}</td>
                                        <td>${c.phone}</td>
                                        <td>
                                            <span class="badge bg-${c.status == 'ACTIVE' ? 'success' : 'secondary'}">${c.status}</span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/customer/detail?id=${c.customerId}"
                                                class="btn btn-sm btn-circle btn-outline-info me-1" title="View Detail">
                                                <i class="bi bi-eye fab"></i>
                                            </a>

                                            <a href="${pageContext.request.contextPath}/admin/customer/update?id=${c.customerId}"
                                                class="btn btn-sm btn-circle btn-outline-primary me-1" title="Update">
                                                <i class="bi bi-pencil fab"></i>
                                            </a>

                                            <c:if test="${c.status == 'ACTIVE'}">
                                            <button type="button" class="btn btn-sm btn-circle btn-outline-danger"
                                                data-bs-toggle="modal" data-bs-target="#deactivateModal${c.customerId}"
                                                title="Deactivate">
                                                <i class="bi bi-dash-circle fab"></i>
                                            </button>

                                            <t:alert id="deactivateModal${c.customerId}">
                                                <jsp:attribute name="title">Confirm Deactivate</jsp:attribute>
                                                <jsp:attribute name="desciption">
                                                    Are you sure you want to deactivate customer
                                                    <strong>${c.name}</strong>? The customer will be set to inactive.
                                                </jsp:attribute>
                                                <jsp:attribute name="action">
                                                    <button type="button" class="btn btn-danger"
                                                        hx-delete="${pageContext.request.contextPath}/admin/customer?id=${c.customerId}"
                                                        hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML"
                                                        data-bs-dismiss="modal">
                                                        Deactivate
                                                    </button>
                                                </jsp:attribute>
                                            </t:alert>
                                            </c:if>

                                            <c:if test="${c.status == 'INACTIVE'}">
                                            <button type="button" class="btn btn-sm btn-circle btn-outline-success"
                                                hx-post="${pageContext.request.contextPath}/admin/customer/activate?id=${c.customerId}"
                                                hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML"
                                                title="Activate">
                                                <i class="bi bi-check-circle fab"></i>
                                            </button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </jsp:body>
                        </t:table>
                    </jsp:body>
                </t:layout>