package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

    @Mock
    private BugRepository bugRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BugService bugService;

    @Test
    void testGetAllBugs() {
        Bug bug1 = new Bug(new User(), "Bug 1", "Desc 1", "image1.jpg", "Open");
        Bug bug2 = new Bug(new User(), "Bug 2", "Desc 2", "image2.jpg", "Closed");

        when(bugRepository.findAll()).thenReturn(List.of(bug1, bug2));

        List<Bug> bugs = bugService.getAllBugs();

        assertEquals(2, bugs.size());
        verify(bugRepository, times(1)).findAll();
    }

    @Test
    void testGetBugById_Found() {
        Bug bug = new Bug(new User(), "Bug 1", "Desc 1", "image1.jpg", "Open");

        when(bugRepository.findById(1L)).thenReturn(Optional.of(bug));

        Optional<Bug> foundBug = bugService.getBugById(1L);

        assertTrue(foundBug.isPresent());
        assertEquals("Bug 1", foundBug.get().getTitle());
        verify(bugRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBugById_NotFound() {
        when(bugRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Bug> foundBug = bugService.getBugById(1L);

        assertFalse(foundBug.isPresent());
        verify(bugRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateBug_Success() {
        User author = new User();
        author.setId(1L);
        Bug bug = new Bug(author, "Bug Title", "Bug Desc", "image.jpg", "Open");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);

        Bug createdBug = bugService.createBug(1L, "Bug Title", "Bug Desc", "image.jpg", "Open");

        assertNotNull(createdBug);
        assertEquals("Bug Title", createdBug.getTitle());
        verify(userRepository, times(1)).findById(1L);
        verify(bugRepository, times(1)).save(any(Bug.class));
    }

    @Test
    void testCreateBug_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                bugService.createBug(1L, "Bug Title", "Bug Desc", "image.jpg", "Open")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(bugRepository, never()).save(any(Bug.class));
    }

    @Test
    void testDeleteBug() {
        doNothing().when(bugRepository).deleteById(1L);

        bugService.deleteBug(1L);

        verify(bugRepository, times(1)).deleteById(1L);
    }
}
