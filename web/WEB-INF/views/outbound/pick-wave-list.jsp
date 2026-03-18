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
        <c:set var="columns" value='${["Wave Code", "GDN Count", "Status", "Created by", "Created at", "Actions"]}' />
        <t:table columns="${columns}">
            <jsp:attribute name="head">
                <form hx-get="${pageContext.request.contextPath}/pick-wave" hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="row g-2 m-0 mt-1">
                    <input type="hidden" name="action" value="list"/>
                    <select class="form-select" name="status" onchange="this.form.requestSubmit()">
                        <option value="">-- All Status --</option>
                        <option value="CREATED" ${param.status == 'CREATED' ? 'selected' : ''}>CREATED</option>
                        <option value="RELEASED" ${param.status == 'RELEASED' ? 'selected' : ''}>RELEASED</option>
                        <option value="IN_PROGRESS" ${param.status == 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
                        <option value="DONE" ${param.status == 'DONE' ? 'selected' : ''}>DONE</option>
                        <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
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
                        <td class="fw-bold">
                            <c:choose>
                                <c:when test="${not empty w.waveCode}">
                                    ${w.waveCode}
                                </c:when>
                                <c:otherwise>
                                    #${w.waveId}
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-center">
                            <span class="badge ${w.gdnCount > 1 ? 'bg-info' : 'bg-secondary'}">
                                ${w.gdnCount != null ? w.gdnCount : 1} GDN${w.gdnCount > 1 ? 's' : ''}
                            </span>
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${w.status == 'CREATED'}">
                                    <span class="badge bg-secondary">Chờ phát hành</span>
                                </c:when>
                                <c:when test="${w.status == 'RELEASED'}">
                                    <span class="badge bg-info">Đã phát hành</span>
                                </c:when>
                                <c:when test="${w.status == 'IN_PROGRESS'}">
                                    <span class="badge bg-warning text-dark">Đang thực hiện</span>
                                </c:when>
                                <c:when test="${w.status == 'DONE'}">
                                    <span class="badge bg-success">Hoàn thành</span>
                                </c:when>
                                <c:when test="${w.status == 'CANCELLED'}">
                                    <span class="badge bg-danger">Đã hủy</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-info">${w.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            ${w.createdByName != null ? w.createdByName : '-'}
                        </td>
                        <td>
                            ${w.createdAtDisplay}
                        </td>
                        <td>
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
