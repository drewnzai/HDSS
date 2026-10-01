package com.andrew.hdss.services;

import com.andrew.hdss.dtos.ChoiceDto;
import com.andrew.hdss.dtos.CreateChoiceRequest;
import com.andrew.hdss.dtos.UpdateChoiceRequest;
import com.andrew.hdss.models.Choice;
import com.andrew.hdss.repositories.ChoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChoiceServiceTest {

    @Mock
    private ChoiceRepository choiceRepository;

    @InjectMocks
    private ChoiceService choiceService;

    private Choice choice1;
    private Choice choice2;

    @BeforeEach
    void setUp() {
        choice1 = Choice.builder()
                .id(1L)
                .listName("SEX")
                .name("male")
                .label("Male")
                .orderIndex(2)
                .build();

        choice2 = Choice.builder()
                .id(2L)
                .listName("SEX")
                .name("female")
                .label("Female")
                .orderIndex(1)
                .build();
    }

    // -------------------------------------------------------------------------
    // getByListName()
    // -------------------------------------------------------------------------

    @Test
    void shouldGetChoicesByListNameAndSortByOrderIndex() {
        when(choiceRepository.findByListName("SEX"))
                .thenReturn(List.of(choice1, choice2));

        List<ChoiceDto> result = choiceService.getByListName("SEX");

        assertThat(result).hasSize(2);

        // choice2 has orderIndex 1, so it should come first.
        assertThat(result.getFirst().id()).isEqualTo(2L);
        assertThat(result.getFirst().name()).isEqualTo("female");
        assertThat(result.get(0).label()).isEqualTo("Female");
        assertThat(result.get(0).orderIndex()).isEqualTo(1);

        assertThat(result.get(1).id()).isEqualTo(1L);
        assertThat(result.get(1).name()).isEqualTo("male");
        assertThat(result.get(1).label()).isEqualTo("Male");
        assertThat(result.get(1).orderIndex()).isEqualTo(2);

        verify(choiceRepository).findByListName("SEX");
    }

    @Test
    void shouldReturnEmptyListWhenListHasNoChoices() {
        when(choiceRepository.findByListName("SEX"))
                .thenReturn(List.of());

        List<ChoiceDto> result = choiceService.getByListName("SEX");

        assertThat(result).isEmpty();

        verify(choiceRepository).findByListName("SEX");
    }

    // -------------------------------------------------------------------------
    // getListNames()
    // -------------------------------------------------------------------------

    @Test
    void shouldGetDistinctListNames() {
        List<String> listNames = List.of("AGE_GROUP", "EDUCATION", "SEX");

        when(choiceRepository.findDistinctListNames())
                .thenReturn(listNames);

        List<String> result = choiceService.getListNames();

        assertThat(result)
                .containsExactly("AGE_GROUP", "EDUCATION", "SEX");

        verify(choiceRepository).findDistinctListNames();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoListNames() {
        when(choiceRepository.findDistinctListNames())
                .thenReturn(List.of());

        List<String> result = choiceService.getListNames();

        assertThat(result).isEmpty();

        verify(choiceRepository).findDistinctListNames();
    }

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateChoice() {
        CreateChoiceRequest request = new CreateChoiceRequest(
                "SEX",
                "male",
                "Male",
                1
        );

        when(choiceRepository.existsByListNameAndName("SEX", "male"))
                .thenReturn(false);

        Choice savedChoice = Choice.builder()
                .id(10L)
                .listName("SEX")
                .name("male")
                .label("Male")
                .orderIndex(1)
                .build();

        when(choiceRepository.save(any(Choice.class)))
                .thenReturn(savedChoice);

        ChoiceDto result = choiceService.create(request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.listName()).isEqualTo("SEX");
        assertThat(result.name()).isEqualTo("male");
        assertThat(result.label()).isEqualTo("Male");
        assertThat(result.orderIndex()).isEqualTo(1);

        verify(choiceRepository)
                .existsByListNameAndName("SEX", "male");

        verify(choiceRepository)
                .save(any(Choice.class));
    }

    @Test
    void shouldCreateChoiceWithCorrectEntityValues() {
        CreateChoiceRequest request = new CreateChoiceRequest(
                "SEX",
                "male",
                "Male",
                1
        );

        when(choiceRepository.existsByListNameAndName("SEX", "male"))
                .thenReturn(false);

        Choice savedChoice = Choice.builder()
                .id(10L)
                .listName("SEX")
                .name("male")
                .label("Male")
                .orderIndex(1)
                .build();

        when(choiceRepository.save(any(Choice.class)))
                .thenReturn(savedChoice);

        choiceService.create(request);

        ArgumentCaptor<Choice> captor =
                ArgumentCaptor.forClass(Choice.class);

        verify(choiceRepository).save(captor.capture());

        Choice saved = captor.getValue();

        assertThat(saved.getListName()).isEqualTo("SEX");
        assertThat(saved.getName()).isEqualTo("male");
        assertThat(saved.getLabel()).isEqualTo("Male");
        assertThat(saved.getOrderIndex()).isEqualTo(1);
    }

    @Test
    void shouldRejectDuplicateChoice() {
        CreateChoiceRequest request = new CreateChoiceRequest(
                "SEX",
                "male",
                "Male",
                1
        );

        when(choiceRepository.existsByListNameAndName("SEX", "male"))
                .thenReturn(true);

        assertThatThrownBy(() -> choiceService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Choice \"male\" already exists in list \"SEX\""
                );

        verify(choiceRepository)
                .existsByListNameAndName("SEX", "male");

        verify(choiceRepository, never())
                .save(any(Choice.class));
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateChoiceWithoutCheckingDuplicateWhenNameIsUnchanged() {
        UpdateChoiceRequest request = new UpdateChoiceRequest(
                "male",
                "Male Person",
                5
        );

        when(choiceRepository.findById(1L))
                .thenReturn(Optional.of(choice1));

        when(choiceRepository.save(any(Choice.class)))
                .thenReturn(choice1);

        ChoiceDto result = choiceService.update(1L, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("male");
        assertThat(result.label()).isEqualTo("Male Person");
        assertThat(result.orderIndex()).isEqualTo(5);

        verify(choiceRepository).findById(1L);

        // Since the name didn't change, duplicate checking is unnecessary.
        verify(choiceRepository, never())
                .existsByListNameAndNameAndIdNot(anyString(), anyString(), anyLong());

        verify(choiceRepository).save(choice1);
    }

    @Test
    void shouldUpdateChoiceWhenNameChangesToNonDuplicate() {
        UpdateChoiceRequest request = new UpdateChoiceRequest(
                "man",
                "Man",
                3
        );

        when(choiceRepository.findById(1L))
                .thenReturn(Optional.of(choice1));

        when(choiceRepository.existsByListNameAndNameAndIdNot(
                "SEX",
                "man",
                1L
        )).thenReturn(false);

        when(choiceRepository.save(any(Choice.class)))
                .thenReturn(choice1);

        ChoiceDto result = choiceService.update(1L, request);

        assertThat(result.name()).isEqualTo("man");
        assertThat(result.label()).isEqualTo("Man");
        assertThat(result.orderIndex()).isEqualTo(3);

        verify(choiceRepository)
                .existsByListNameAndNameAndIdNot(
                        "SEX",
                        "man",
                        1L
                );

        verify(choiceRepository).save(choice1);
    }

    @Test
    void shouldRejectUpdateWhenNewNameAlreadyExists() {
        UpdateChoiceRequest request = new UpdateChoiceRequest(
                "female",
                "Female",
                1
        );

        when(choiceRepository.findById(1L))
                .thenReturn(Optional.of(choice1));

        when(choiceRepository.existsByListNameAndNameAndIdNot(
                "SEX",
                "female",
                1L
        )).thenReturn(true);

        assertThatThrownBy(() ->
                choiceService.update(1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Choice \"female\" already exists in list \"SEX\""
                );

        verify(choiceRepository)
                .existsByListNameAndNameAndIdNot(
                        "SEX",
                        "female",
                        1L
                );

        verify(choiceRepository, never())
                .save(any(Choice.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentChoice() {
        UpdateChoiceRequest request = new UpdateChoiceRequest(
                "male",
                "Male",
                1
        );

        when(choiceRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                choiceService.update(99L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Choice not found: 99");

        verify(choiceRepository).findById(99L);

        verify(choiceRepository, never())
                .save(any(Choice.class));
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteExistingChoice() {
        when(choiceRepository.existsById(1L))
                .thenReturn(true);

        choiceService.delete(1L);

        verify(choiceRepository).existsById(1L);
        verify(choiceRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonexistentChoice() {
        when(choiceRepository.existsById(99L))
                .thenReturn(false);

        assertThatThrownBy(() ->
                choiceService.delete(99L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Choice not found: 99");

        verify(choiceRepository).existsById(99L);

        verify(choiceRepository, never())
                .deleteById(anyLong());
    }
}