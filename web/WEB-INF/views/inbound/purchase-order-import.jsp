<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<t:layout title="Import Purchase Order">
    <jsp:attribute name="actions">
        <a href="${pageContext.request.contextPath}/purchase-orders" class="btn btn-outline-secondary">Back to List</a>
    </jsp:attribute>
    <jsp:body>
        <div class="row justify-content-center mt-4">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header fw-semibold bg-primary text-white">Upload Excel File</div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/purchase-orders" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="action" value="processImport">
                            
                            <div class="mb-4">
                                <label for="file" class="form-label fw-bold">Select Excel file (.xlsx, .xls)</label>
                                <input class="form-control" type="file" id="file" name="file" accept=".xlsx, .xls" required>
                            </div>
                            
                            <div class="alert alert-info py-2">
                                <strong>Required Excel Format:</strong>
                                <p class="mb-1 text-muted small">Please ensure your Excel file contains the following columns in row 1:</p>
                                <ul class="mb-0 small text-muted">
                                    <li><strong>PO Number</strong> (Required, max 20 chars)</li>
                                    <li><strong>Supplier ID</strong> (Required, integer)</li>
                                    <li><strong>Expected Delivery Date</strong> (Required, yyyy-MM-dd format, must be in future)</li>
                                    <li><strong>Note</strong> (Optional)</li>
                                    <li><strong>Variant ID</strong> (Required, integer)</li>
                                    <li><strong>Quantity</strong> (Required, > 0)</li>
                                    <li><strong>Unit Price</strong> (Required, > 0)</li>
                                </ul>
                                <p class="small text-muted mt-2 mb-0">All rows will be imported as lines of a single Purchase Order matching the fields above. Data should start from row 2.</p>
                            </div>

                            <div class="text-right">
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-upload mr-1"></i> Import
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
