<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<t:layout title="Import Sale Order">
    <jsp:attribute name="actions">
        <a href="${pageContext.request.contextPath}/sales-orders" class="btn btn-outline-secondary">
            Back to List
        </a>
    </jsp:attribute>
    <jsp:body>
        <div class="row justify-content-center mt-4">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header fw-semibold bg-primary text-white">
                        Upload Excel File
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/sales-orders" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="action" value="processImport">

                            <div class="mb-4">
                                <label for="file" class="form-label fw-bold">Select Excel file (.xlsx, .xls)</label>
                                <input class="form-control" type="file" id="file" name="file" accept=".xlsx, .xls" required>
                            </div>

                            <div class="alert alert-info py-2">
                                <strong>Required Excel Format:</strong>
                                <p class="mb-1 text-muted small">
                                    Please ensure your Excel file contains the following columns (starting from row 1):
                                </p>
                                <ul class="mb-0 small text-muted">
                                    <li><strong>A: SO Number</strong> (Required, max 20 chars)</li>
                                    <li><strong>B: Customer Code</strong> (Required, mapped to customer.code)</li>
                                    <li><strong>C: Ship To Address</strong> (Required)</li>
                                    <li><strong>D: Requested Ship Date</strong> (Required, yyyy-MM-dd or Excel date, must be after today)</li>
                                    <li><strong>E: Variant SKU</strong> (Required, mapped to product_variant.variant_sku)</li>
                                    <li><strong>F: Quantity</strong> (Required, &gt; 0)</li>
                                    <li><strong>G: Unit Price</strong> (Optional, &gt; 0 if provided)</li>
                                </ul>
                                <p class="small text-muted mt-2 mb-0">
                                    Row 1 contains the Sale Order header (A..D) and the first line (E..G).<br/>
                                    Subsequent rows (2, 3, ...) contain additional lines (columns E, F, G).
                                </p>
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

