<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="t" tagdir="/WEB-INF/tags/" %>

            <t:layout title="Create Supplier">
                <jsp:attribute name="actions">
                    <t:link url="${pageContext.request.contextPath}/admin/supplier" variant="split" color="dark"
                        icon="chevron-left">
                        Go back
                    </t:link>
                </jsp:attribute>

                <jsp:body>
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <form method="POST" action="${pageContext.request.contextPath}/admin/supplier/create"
                                class="m-0">

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="name" class="form-label">
                                            Name <span class="text-danger">*</span>
                                        </label>
                                        <input type="text" class="form-control" id="name" name="name" required
                                            value="${supplier.name}" placeholder="Supplier Name">
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="email" class="form-label">
                                            Email <span class="text-danger">*</span>
                                        </label>
                                        <input type="email" class="form-control" id="email" name="email"
                                            value="${supplier.email}" placeholder="contact@supplier.com" required>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="phone" class="form-label">
                                            Phone <span class="text-danger">*</span>
                                        </label>
                                        <input type="tel" class="form-control" id="phone" name="phone"
                                            value="${supplier.phone}" placeholder="0123456789" pattern="0[0-9]{9}"
                                            title="Số điện thoại phải bắt đầu bằng số 0 và có độ dài 10 chữ số" required>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <label for="address" class="form-label">Address</label>
                                    <textarea class="form-control" id="address" name="address" rows="2"
                                        placeholder="Supplier Address" maxlength="255">${supplier.address}</textarea>
                                </div>

                                <div class="d-flex justify-content-end gap-2">
                                    <t:link url="${pageContext.request.contextPath}/admin/supplier" icon="x-circle"
                                        color="dark" variant="split">
                                        Cancel
                                    </t:link>
                                    <t:button type="submit" variant="split" icon="check-circle" color="primary">
                                        Save
                                    </t:button>
                                </div>
                            </form>
                        </div>
                    </div>
                </jsp:body>
            </t:layout>