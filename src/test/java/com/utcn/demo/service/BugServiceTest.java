package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.UserRepository;
import com.utcn.demo.repository.TagRepository;
import com.utcn.demo.repository.BugTagRepository;
import com.utcn.demo.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

    @Mock private BugRepository bugRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagRepository tagRepository;
    @Mock private BugTagRepository bugTagRepository;
    @Mock private VoteService voteService;
    @Mock private CommentService commentService;
    @Mock private CommentRepository commentRepository;

    @InjectMocks private BugService bugService;

    private User user;
    private final List<Integer> tagIds = List.of(1, 2);

    @BeforeEach
    void setUp() {
        // Common stub: userRepository must find the user for create/delete
        user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void testGetAllBugs() {
        Bug b1 = new Bug(user, "A", "D", "img", "Open");
        Bug b2 = new Bug(user, "B", "E", "img", "Closed");
        when(bugRepository.findAll()).thenReturn(List.of(b1, b2));

        List<Bug> all = bugService.getAllBugs();

        assertEquals(2, all.size());
        verify(bugRepository).findAll();
    }

    @Test
    void testGetBugById_Found() {
        Bug b = new Bug(user, "A", "D", "img", "Open");
        when(bugRepository.findById(5L)).thenReturn(Optional.of(b));

        Optional<Bug> result = bugService.getBugById(5L);

        assertTrue(result.isPresent());
        assertEquals("A", result.get().getTitle());
        verify(bugRepository).findById(5L);
    }

    @Test
    void testGetBugById_NotFound() {
        when(bugRepository.findById(5L)).thenReturn(Optional.empty());

        Optional<Bug> result = bugService.getBugById(5L);

        assertFalse(result.isPresent());
        verify(bugRepository).findById(5L);
    }

    @Test
    void testCreateBug_Success() {
        // Arrange
        String title = "BugTitle", desc = "Desc", steps = "S1,S2", priority = "HIGH";
        when(bugRepository.save(any(Bug.class)))
                .thenAnswer(invocation -> {
                    Bug arg = invocation.getArgument(0);
                    arg.setId(99L);
                    return arg;
                });

        // Act
        Bug created = bugService.createBug(
                1L,        // userId
                title,
                desc,
                steps,
                priority,
                tagIds
        );

        // Assert
        assertNotNull(created);
        assertEquals(99L, created.getId());
        assertEquals(title, created.getTitle());
        verify(userRepository).findById(1L);
        verify(bugRepository).save(any(Bug.class));
        // and for tags:
        verify(bugTagRepository, times(tagIds.size())).save(any());
    }

    @Test
    void testCreateBug_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bugService.createBug(1L, "T", "D", "IMG", "OPEN", tagIds)
        );

        assertEquals("User not found", ex.getMessage());
        verify(bugRepository, never()).save(any());
    }

    @Test
    void testDeleteBug_Success() {
        // Arrange: service will load the Bug and check its author
        Bug existing = new Bug();
        existing.setId(55L);
        existing.setAuthor(user);  // <— setter corect: setAuthor, nu setReporter :contentReference[oaicite:2]{index=2}
        when(bugRepository.findById(55L)).thenReturn(Optional.of(existing));

        // stub all downstream deletes
        doNothing().when(bugTagRepository).deleteByBugId(55L);
        doNothing().when(voteService).deleteVotesForBug(55L);
        doNothing().when(commentRepository).deleteByBugId(55L);
        doNothing().when(bugRepository).deleteById(55L);

        // Act: apelul corect cu doi parametri Long
        bugService.deleteBug(55L, 1L);

        // Assert: toate metodele de delete au fost chemate
        verify(bugTagRepository).deleteByBugId(55L);
        verify(voteService).deleteVotesForBug(55L);
        verify(commentRepository).deleteByBugId(55L);
        verify(bugRepository).deleteById(55L);
    }

    @Test
    void testDeleteBug_ForbiddenWhenNotAuthor() {
        // Arrange: bug creat de alt user
        Bug existing = new Bug();
        existing.setId(66L);
        User other = new User(); other.setId(2L);
        existing.setAuthor(other);
        when(bugRepository.findById(66L)).thenReturn(Optional.of(existing));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bugService.deleteBug(66L, 1L)
        );
        assertEquals("Only the bug creator can delete their bug", ex.getMessage());
        // niciun delete downstream nu trebuie apelat
        verify(bugRepository, never()).deleteById(any());
    }
}
