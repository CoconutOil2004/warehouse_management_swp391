import java.util.Collections;
import java.util.List;

import org.junit.Test;
import dto.PageResponseDTO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Nhóm 5 — 3 method (Lombok {@code @Data}): {@link PageResponseDTO#getItems()},
 * {@link PageResponseDTO#getTotalItems()}, {@link PageResponseDTO#getCurrentPage()}.
 * 7 testcase (builder set thêm totalPages/pageSize để compile, không assert riêng).
 */
public class Person5PageResponseDTOTest {

    @Test
    public void getItems_twoElements() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(List.of("x", "y"))
                .totalItems(2L).totalPages(1L).currentPage(1L).pageSize(10L)
                .build();
        assertNotNull(p.getItems());
        assertEquals(2, p.getItems().size());
    }

    @Test
    public void getItems_emptyList() {
        PageResponseDTO<Integer> p = PageResponseDTO.<Integer>builder()
                .items(Collections.emptyList())
                .totalItems(0L).totalPages(0L).currentPage(1L).pageSize(20L)
                .build();
        assertTrue(p.getItems().isEmpty());
    }

    @Test
    public void getItems_singleElement() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(List.of("only"))
                .totalItems(1L).totalPages(1L).currentPage(1L).pageSize(10L)
                .build();
        assertEquals(1, p.getItems().size());
        assertEquals("only", p.getItems().get(0));
    }

    @Test
    public void getTotalItems_largeList() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(List.of("a"))
                .totalItems(500L).totalPages(50L).currentPage(1L).pageSize(10L)
                .build();
        assertEquals(500L, p.getTotalItems());
    }

    @Test
    public void getTotalItems_zero() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(Collections.emptyList())
                .totalItems(0L).totalPages(0L).currentPage(1L).pageSize(10L)
                .build();
        assertEquals(0L, p.getTotalItems());
    }

    @Test
    public void getCurrentPage_firstPage() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(List.of("a"))
                .totalItems(1L).totalPages(1L).currentPage(1L).pageSize(10L)
                .build();
        assertEquals(1L, p.getCurrentPage());
    }

    @Test
    public void getCurrentPage_middlePage() {
        PageResponseDTO<String> p = PageResponseDTO.<String>builder()
                .items(List.of("a"))
                .totalItems(100L).totalPages(10L).currentPage(5L).pageSize(10L)
                .build();
        assertEquals(5L, p.getCurrentPage());
    }
}
