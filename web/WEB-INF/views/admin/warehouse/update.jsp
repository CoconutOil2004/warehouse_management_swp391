<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="t" tagdir="/WEB-INF/tags/" %>

            <t:layout title="Update Warehouse">
                <jsp:attribute name="actions">
                    <t:link url="${pageContext.request.contextPath}/admin/warehouse" variant="split" color="dark"
                        icon="chevron-left">
                        Go back
                    </t:link>
                </jsp:attribute>

                <jsp:body>
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger">${error}</div>
                            </c:if>
                            
                            <c:if test="${not empty success}">
                                <div class="alert alert-success">${success}</div>
                            </c:if>

                            <form method="POST" action="${pageContext.request.contextPath}/admin/warehouse/update" class="m-0">

                                <input type="hidden" name="id" value="${warehouse.warehouseId}">

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="code" class="form-label">
                                            Code <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="code" name="code" required
                                            value="${warehouse.code}" placeholder="Example: WH001">
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="name" class="form-label">
                                            Name <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="name" name="name" required
                                            value="${warehouse.name}" placeholder="Warehouse Name">
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="status" class="form-label">Status</label>
                                        <select class="form-select" id="status" name="status">
                                            <option value="ACTIVE" ${warehouse.status=='ACTIVE' ? 'selected' : '' }>
                                                Active</option>
                                            <option value="INACTIVE" ${warehouse.status=='INACTIVE' ? 'selected' : '' }>
                                                Inactive</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <label for="address" class="form-label">Address</label>
                                    <textarea class="form-control" id="address" name="address" rows="2"
                                        placeholder="Warehouse Address">${warehouse.address}</textarea>
                                </div>

                                <div class="d-flex justify-content-end gap-2">
                                    <t:link url="${pageContext.request.contextPath}/admin/warehouse" icon="x-circle"
                                        color="dark" variant="split">
                                        Cancel
                                    </t:link>
                                    <t:button type="submit" variant="split" icon="check-circle" color="primary">
                                        Save Changes
                                    </t:button>
                                </div>
                            </form>
                        </div>
                    </div>
                </jsp:body>
            </t:layout>