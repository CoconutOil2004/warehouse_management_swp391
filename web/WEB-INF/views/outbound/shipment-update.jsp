<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <t:layout title="Update Shipment: ${shipment.shipmentNumber}">
                <div class="row justify-content-center">
                    <div class="col-lg-7">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3 bg-gradient-warning text-white">
                                <h6 class="m-0 font-weight-bold"><i class="fas fa-sync-alt mr-2"></i>Cập nhật Trạng thái
                                    Vận chuyển</h6>
                            </div>
                            <div class="card-body">
                                <form action="${pageContext.request.contextPath}/shipment" method="post"
                                    id="updateStatusForm">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${shipment.shipmentId}">

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Trạng thái hiện tại</label>
                                        <div class="p-3 bg-light rounded mb-2 border-left-warning h5 font-weight-bold">
                                            <c:choose>
                                                <c:when test="${shipment.status == 'CREATED'}">Mới tạo (CREATED)
                                                </c:when>
                                                <c:when test="${shipment.status == 'PICKED_UP'}">Đã lấy hàng (PICKED_UP)
                                                </c:when>
                                                <c:when test="${shipment.status == 'IN_TRANSIT'}">Đang giao hàng
                                                    (IN_TRANSIT)</c:when>
                                                <c:when test="${shipment.status == 'DELIVERED'}">Giao thành công
                                                    (DELIVERED)</c:when>
                                                <c:when test="${shipment.status == 'CANCELLED'}">Hủy đơn/Thất bại
                                                    (CANCELLED)</c:when>
                                                <c:otherwise>${shipment.status}</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-12">
                                            <label class="form-label font-weight-bold">Thay đổi trạng thái thành</label>
                                            <select class="form-control form-control-lg border-primary" name="status"
                                                id="statusSelect" required>
                                                <option value="CREATED" ${shipment.status=='CREATED' ? 'selected' : ''
                                                    }>Mới tạo (CREATED)</option>
                                                <option value="PICKED_UP" ${shipment.status=='PICKED_UP' ? 'selected'
                                                    : '' }>Đã lấy hàng (PICKED_UP)</option>
                                                <option value="IN_TRANSIT" ${shipment.status=='IN_TRANSIT' ? 'selected'
                                                    : '' }>Đang giao hàng (IN_TRANSIT)</option>
                                                <option value="DELIVERED" ${shipment.status=='DELIVERED' ? 'selected'
                                                    : '' }>Giao thành công (DELIVERED)</option>
                                                <option value="CANCELLED" ${shipment.status=='CANCELLED' ? 'selected'
                                                    : '' }>Hủy đơn/Thất bại (CANCELLED)</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-12">
                                            <label class="form-label font-weight-bold">Cập nhật Đơn vị vận
                                                chuyển</label>
                                            <select class="form-control" name="carrierId" ${shipment.status !='CREATED'
                                                ? 'disabled' : '' }>
                                                <c:forEach var="c" items="${carriers}">
                                                    <option value="${c.carrierId}" ${shipment.carrierId==c.carrierId
                                                        ? 'selected' : '' }>${c.name}</option>
                                                </c:forEach>
                                            </select>
                                            <c:if test="${shipment.status != 'CREATED'}">
                                                <input type="hidden" name="carrierId" value="${shipment.carrierId}">
                                            </c:if>
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Tracking Code (Mã vận đơn) <span
                                                class="text-danger">*</span></label>
                                        <input type="text" class="form-control" name="trackingCode"
                                            value="${shipment.trackingCode}" placeholder="Cập nhật mã vận đơn..."
                                            required>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Ghi chú thực hiện</label>
                                        <textarea class="form-control" name="note" rows="3"
                                            placeholder="Add status change reason or delivery notes...">${shipment.note}</textarea>
                                    </div>

                                    <div class="alert alert-info small">
                                        <i class="fas fa-info-circle mr-1"></i> Thay đổi trạng thái thành <b>ĐANG GIAO
                                            (IN TRANSIT)</b> hoặc
                                        <b>ĐÃ GIAO (DELIVERED)</b> sẽ tự động ghi lại mốc thời gian hiện tại.
                                    </div>

                                    <hr>
                                    <div class="d-flex justify-content-between">
                                        <a href="${pageContext.request.contextPath}/shipment?action=detail&id=${shipment.shipmentId}"
                                            class="btn btn-light px-4">Hủy bỏ</a>
                                        <button type="button" onclick="confirmUpdate()"
                                            class="btn btn-warning px-5 font-weight-bold shadow-sm">
                                            <i class="fas fa-save mr-1"></i> Lưu thay đổi
                                        </button>
                                    </div>
                                </form>
                                <script>
                                    function confirmUpdate() {
                                        const statusText = document.getElementById('statusSelect').options[document.getElementById('statusSelect').selectedIndex].text;
                                        if (confirm("Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng thành: " + statusText + "?")) {
                                            document.getElementById('updateStatusForm').submit();
                                        }
                                    }
                                </script>
                            </div>
                        </div>
                    </div>
                </div>
            </t:layout>