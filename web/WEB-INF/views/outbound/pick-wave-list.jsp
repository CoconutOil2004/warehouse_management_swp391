<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<t:layout title="Pick Wave">
    <jsp:attribute name="actions">
        <c:set var="roleNames" value="${sessionScope.USER != null ? sessionScope.USER.roleNames : ''}"/>
        <c:if test="${fn:contains(roleNames, 'ADMIN') || fn:contains(roleNames, 'WAREHOUSE_MANAGER')}">
            <t:link url="${pageContext.request.contextPath}/pick-wave?action=create" color="success" variant="split" icon="plus-lg">
                Create
            </t:link>
        </c:if>
    </jsp:attribute>

    <jsp:body>
        <c:set var="columns" value='${["Wave ID", "GDN Number", "Status", "Created by", "Created at", "Actions"]}' />
        <t:table columns="${columns}">
            <jsp:attribute name="head">
                <form hx-get="${pageContext.request.contextPath}/pick-wave" hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="row g-2 m-0 mt-1">
                    <input type="hidden" name="action" value="list"/>
                    <select class="form-select" name="status" onchange="this.form.requestSubmit()">
                        <option value="">-- All Status --</option>
                        <option value="CREATED" ${param.status == 'CREATED' ? 'selected' : ''}>CREATED</option>
                    </select>
                </form>
            </jsp:attribute>

            <jsp:attribute name="foot">
                <t:pagination
                    page="${page}"
                    pages="${pages}"
                    size="${size}"
                    total="${total}"
                    url="${pageContext.request.contextPath}/pick-wave"
                    include="[name='status']"
                />
            </jsp:attribute>

            <jsp:body>
                <c:forEach var="w" items="${waves}">
                    <tr>
                        <td>${w.waveId}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${w.gdnId}" class="fw-semibold text-decoration-none">
                                ${w.gdnNumber}
                            </a>
                        </td>
                        <td>
                            <span class="badge ${w.status == 'CREATED' ? 'bg-secondary' : 'bg-info'}">
                                ${w.status}
                            </span>
                        </td>
                        <td>
                            ${w.createdByName != null ? w.createdByName : '-'}
                        </td>
                        <td>
                            ${w.createdAtDisplay}
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/pick-task?action=assign&waveId=${w.waveId}" 
                               class="btn btn-sm btn-circle btn-outline-primary me-1" title="Assign tasks">
                                <i class="bi bi-list-check"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/pick-wave?action=detail&id=${w.waveId}" 
                               class="btn btn-sm btn-circle btn-outline-secondary" title="Detail">
                                <i class="bi bi-eye"></i>
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty waves}">
                    <tr><td colspan="6" class="text-center py-4 text-muted">No waves found.</td></tr>
                </c:if>
            </jsp:body>
        </t:table>
    </jsp:body>
</t:layout>
