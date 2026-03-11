package service;

import dao.UserDAO;
import dto.PageResponseDTO;
import dto.UserDTO;
import dto.UserRequest;
import java.sql.SQLException;
import java.util.List;
import model.User;
import util.PasswordUtil;

public class UserService {
    private final UserDAO userDao = new UserDAO();
    private final dao.RoleDAO roleDao = new dao.RoleDAO();

    public PageResponseDTO<UserDTO> getPagedList(String search, String sort, long page, long size, Long roleId, String status, Boolean isDeleted, Long excludeUserId) throws SQLException {
        long total = userDao.getPageCount(search, roleId, status, isDeleted, excludeUserId);
        List<User> users = userDao.getList(search, sort, page, size, roleId, status, isDeleted, excludeUserId);
        
        List<UserDTO> dtos = users.stream()
            .map(this::mapToDTO)
            .toList();

        return PageResponseDTO.<UserDTO>builder()
            .items(dtos)
            .totalItems(total)
            .totalPages((total + size - 1) / size)
            .currentPage(page)
            .pageSize(size)
            .build();
    }

    public UserDTO getById(Long id) throws SQLException {
        User u = userDao.getById(id);
        return u != null ? mapToDTO(u) : null;
    }

    public List<model.Role> getRolesDetailByUserId(Long id) throws SQLException {
        return userDao.getRolesDetailByUserId(id);
    }

    public List<Long> getRolesByUserId(Long id) throws SQLException {
        return userDao.getRolesByUserId(id);
    }

    public java.util.Map<model.Role, java.util.List<model.Permission>> getRolePermissionsMap(Long userId) throws SQLException {
        var roles = userDao.getRolesDetailByUserId(userId);
        var rolePermissionsMap = new java.util.LinkedHashMap<model.Role, java.util.List<model.Permission>>();
        for (var role : roles) {
            var permissions = roleDao.getPermissionsDetailByRoleId(role.getRoleId());
            rolePermissionsMap.put(role, permissions);
        }
        return rolePermissionsMap;
    }

    public void create(UserRequest request, Long createdBy) throws SQLException {
        User u = new User();
        u.setUsername(request.getUsername());
        u.setFullName(request.getFullName());
        u.setEmail(request.getEmail());
        u.setPhone(request.getPhone());
        u.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        u.setWarehouseId(request.getWarehouseId());
        u.setCreatedBy(createdBy);
        
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new util.ValidationException("Mật khẩu không được để trống");
        }
        u.setPasswordHash(PasswordUtil.hashPassword(request.getPassword()));
        
        validateUser(u, true);
        
        if (userDao.create(u)) {
            if (request.getRoleIds() != null) {
                userDao.setUserRoles(u.getUserId(), request.getRoleIds());
            }
        }
    }

    public void update(Long id, UserRequest request) throws SQLException {
        User u = userDao.getById(id);
        if (u == null) throw new util.ValidationException("Người dùng không tồn tại");
        
        u.setFullName(request.getFullName());
        u.setEmail(request.getEmail());
        u.setPhone(request.getPhone());
        u.setStatus(request.getStatus());
        u.setWarehouseId(request.getWarehouseId());
        
        validateUser(u, false);
        
        if (userDao.update(u)) {
            userDao.setUserRoles(id, request.getRoleIds());
        }
    }

    private void validateUser(User u, boolean isCreate) throws SQLException {
        if (u.getUsername() != null && (u.getUsername().isBlank() || u.getUsername().length() < 3)) {
            throw new util.ValidationException("Tên đăng nhập phải có ít nhất 3 ký tự");
        }
        if (u.getEmail() == null || u.getEmail().isBlank() || !u.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new util.ValidationException("Email không hợp lệ");
        }
        if (u.getFullName() == null || u.getFullName().isBlank()) {
            throw new util.ValidationException("Họ và tên không được để trống");
        }

        if (isCreate) {
             if (userDao.usernameExists(u.getUsername(), null)) {
                 throw new util.ValidationException("Tên đăng nhập đã tồn tại");
             }
        }
        
        if (userDao.emailExists(u.getEmail(), isCreate ? null : u.getUserId())) {
            throw new util.ValidationException("Email đã tồn tại");
        }
    }

    public boolean delete(Long id) throws SQLException {
        return userDao.delete(id);
    }

    public boolean restore(Long id) throws SQLException {
        return userDao.restore(id);
    }

    private UserDTO mapToDTO(User u) {
        return UserDTO.builder()
            .userId(u.getUserId())
            .username(u.getUsername())
            .fullName(u.getFullName())
            .email(u.getEmail())
            .phone(u.getPhone())
            .status(u.getStatus())
            .warehouseId(u.getWarehouseId())
            .roleNames(u.getRoleNames())
            .createdAt(u.getCreatedAt())
            .isDeleted(u.getIsDeleted())
            .build();
    }
}
