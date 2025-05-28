package com.utcn.demo.service;

import com.utcn.demo.entity.Ban;
import com.utcn.demo.repository.BanRepository;
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
class BanServiceTest {

    @Mock
    private BanRepository banRepository;

    @InjectMocks
    private BanService banService;

    @Test
    void testGetAllBans() {
        Ban ban1 = new Ban();
        Ban ban2 = new Ban();

        when(banRepository.findAll()).thenReturn(List.of(ban1, ban2));

        List<Ban> bans = banService.getAllBans();

        assertEquals(2, bans.size());
        verify(banRepository, times(1)).findAll();
    }

    @Test
    void testCreateBan() {
        Ban ban = new Ban();
        ban.setReason("Spam");

        when(banRepository.save(ban)).thenReturn(ban);

        Ban createdBan = banService.createBan(ban);

        assertNotNull(createdBan);
        assertEquals("Spam", createdBan.getReason());
        verify(banRepository, times(1)).save(ban);
    }

    @Test
    void testDeleteBan() {
        doNothing().when(banRepository).deleteById(1);

        banService.deleteBan(1);

        verify(banRepository, times(1)).deleteById(1);
    }

    @Test
    void testUpdateBan_Success() {
        Ban existingBan = new Ban();
        existingBan.setId(1);
        existingBan.setReason("Old Reason");

        Ban updatedBan = new Ban();
        updatedBan.setReason("New Reason");

        when(banRepository.findById(1)).thenReturn(Optional.of(existingBan));
        when(banRepository.save(any(Ban.class))).thenReturn(existingBan);

        Ban result = banService.updateBan(1, updatedBan);

        assertNotNull(result);
        assertEquals("New Reason", result.getReason());
        verify(banRepository).findById(1);
        verify(banRepository).save(existingBan);
    }

    @Test
    void testUpdateBan_NotFound() {
        Ban updatedBan = new Ban();
        updatedBan.setReason("New Reason");

        when(banRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                banService.updateBan(1, updatedBan)
        );

        assertEquals("Ban not found", exception.getMessage());
        verify(banRepository).findById(1);
        verify(banRepository, never()).save(any(Ban.class));
    }
}
