package com.airbnbspa.service;

import com.airbnbspa.dto.EquipmentDTO;
import com.airbnbspa.entity.Equipment;
import com.airbnbspa.repository.EquipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipment> getAllActive() {
        return equipmentRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Equipment> getAll() {
        return equipmentRepository.findAll();
    }

    public Equipment createEquipment(EquipmentDTO dto) {
        Equipment equipment = Equipment.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .active(true)
                .build();
        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long id, EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));

        if (dto.getName() != null) equipment.setName(dto.getName());
        if (dto.getDescription() != null) equipment.setDescription(dto.getDescription());
        if (dto.getIcon() != null) equipment.setIcon(dto.getIcon());
        equipment.setActive(dto.isActive());

        return equipmentRepository.save(equipment);
    }

    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));
    }

    // DTO convenience methods for controllers
    public List<EquipmentDTO> getAllActiveDTOs() {
        return equipmentRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getAllDTOs() {
        return equipmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EquipmentDTO createDTO(EquipmentDTO dto) {
        Equipment equipment = createEquipment(dto);
        return toDTO(equipment);
    }

    public EquipmentDTO updateDTO(Long id, EquipmentDTO dto) {
        Equipment equipment = updateEquipment(id, dto);
        return toDTO(equipment);
    }

    public EquipmentDTO getByIdDTO(Long id) {
        Equipment equipment = findById(id);
        return toDTO(equipment);
    }

    private EquipmentDTO toDTO(Equipment equipment) {
        return EquipmentDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .icon(equipment.getIcon())
                .active(equipment.isActive())
                .build();
    }
}