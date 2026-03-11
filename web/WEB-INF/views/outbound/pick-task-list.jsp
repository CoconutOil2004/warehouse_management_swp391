<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<t:layout title="My Pick Tasks">
  <jsp:body>
    <c:set
      var="columns"
      value='${["ID", "GDN", "Sales Order", "Status", "Assigned At", "Completed At", "Actions"]}'
    />
    <t:table columns="${columns}">
      <jsp:attribute name="head">
        <form
          hx-get="${pageContext.request.contextPath}/pick-task"
          hx-target="#wrapper"
          hx-select="#wrapper"
          hx-swap="outerHTML"
          hx-push-url="true"
          class="m-0 d-flex gap-2"
        >
          <input type="hidden" name="action" value="myTasks" />
          <select class="form-select" name="status">
            <option value="">-- All Status --</option>
            <option value="ASSIGNED" ${param.status=='ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
            <option value="IN_PROGRESS" ${param.status=='IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
            <option value="COMPLETED" ${param.status=='COMPLETED' ? 'selected' : ''}>COMPLETED</option>
          </select>
          <button type="submit" class="btn btn-primary d-flex gap-2 align-items-center">
            <i class="bi bi-filter"></i>
            <span>Filter</span>
          </button>
          <a
            href="${pageContext.request.contextPath}/pick-task?action=myTasks"
            class="btn btn-secondary d-flex gap-2 align-items-center"
          >
            <i class="bi bi-arrow-clockwise"></i>
            <span>Reset</span>
          </a>
        </form>
      </jsp:attribute>

      <jsp:attribute name="foot">
        <t:pagination
          page="${page}"
          pages="${pages}"
          size="${size}"
          total="${total}"
          url="${pageContext.request.contextPath}/pick-task"
          include="[name='status'], [name='action']"
        />
      </jsp:attribute>

      <jsp:body>
        <c:forEach var="task" items="${tasks}">
          <tr>
            <td class="text-center">${task.pickTaskId}</td>
            <td>
              <a
                href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${task.gdnId}"
                class="fw-bold text-decoration-none"
              >
                ${task.gdnNumber}
              </a>
            </td>
            <td>
              <c:choose>
                <c:when test="${not empty task.soId}">
                  <a
                    href="${pageContext.request.contextPath}/sales-orders?action=detail&id=${task.soId}"
                    class="text-decoration-none"
                  >
                    ${task.soNumber}
                  </a>
                </c:when>
                <c:otherwise>
                  ${task.soNumber != null ? task.soNumber : '-'}
                </c:otherwise>
              </c:choose>
            </td>
            <td class="text-center">
              <span
                class="badge rounded-pill ${task.status == 'ASSIGNED' ? 'bg-info' : (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-success')}"
              >
                ${task.status}
              </span>
            </td>
            <td class="text-center text-muted small">
              <c:out value="${task.assignedAt}" />
            </td>
            <td class="text-center text-muted small">
              <c:out value="${task.completedAt != null ? task.completedAt : '-'}" />
            </td>
            <td class="text-center">
              <a
                href="${pageContext.request.contextPath}/pick-task?action=detail&id=${task.pickTaskId}"
                class="btn btn-sm btn-circle btn-outline-info"
                title="View Detail"
              >
                <i class="bi bi-eye"></i>
              </a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty tasks}">
          <tr>
            <td colspan="7" class="text-center py-4 text-muted">
              No pick tasks found.
            </td>
          </tr>
        </c:if>
      </jsp:body>
    </t:table>
  </jsp:body>
</t:layout>
