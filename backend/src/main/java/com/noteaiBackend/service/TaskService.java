package com.noteaiBackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.noteaiBackend.dto.ClassFindByTeacherIdDTO;
import com.noteaiBackend.dto.TaskFindByTeacherIdWithClassDTO;
import com.noteaiBackend.dto.TaskWithNoteId;
import com.noteaiBackend.entity.Task;
import com.noteaiBackend.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository repository;

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Optional<Task> findById(Integer id) {
        return repository.findById(id);
    }

    public Task save(Task entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<ClassFindByTeacherIdDTO> findByTeacherId(Integer id) {
        return repository.findByTeacherId(id);
    }

    public List<TaskFindByTeacherIdWithClassDTO> TaskFindByTeacherIdWithClass(Integer id) {
        return repository.findByTeacherIdWithClass(id);
    }

    public List<TaskFindByTeacherIdWithClassDTO> findByClassIdWithStats(Integer classId) {
        return repository.findByClassIdWithStats(classId);
    }

    public List<Task> findByClassId(Integer classId) {
        return repository.findByClassId(classId);
    }

    public List<TaskWithNoteId> findByClassIdWithNoteId(Integer classId, Integer noteId) {
        return repository.findByClassIdWithNoteId(classId,noteId);
    }
}
