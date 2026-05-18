package com.noteaiBackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noteaiBackend.dto.ApiResponse;
import com.noteaiBackend.dto.ClassFindByTeacherIdDTO;
import com.noteaiBackend.dto.TaskFindByTeacherIdWithClassDTO;
import com.noteaiBackend.dto.TaskWithNoteId;
import com.noteaiBackend.entity.Task;
import com.noteaiBackend.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService service;

    @GetMapping
    public List<Task> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    创建课程笔记任务
    @PostMapping
    public Task create(@RequestBody Task entity) {
        return service.save(entity);
    }

//    更新课堂笔记作业
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Integer id, @RequestBody Task entity) {
        return service.findById(id).map(existing -> {
            entity.setId(id);
            return ResponseEntity.ok(service.save(entity));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("byTeacherId/{id}")
    public ResponseEntity<ApiResponse<List<ClassFindByTeacherIdDTO>>> getByTeacherId(@PathVariable Integer id) {
        try {
            List<ClassFindByTeacherIdDTO> res = service.findByTeacherId(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

//    与id为{id}有关的教师课程信息（courseManagement.vue）
    @GetMapping("byTeacherIdWithClass/{id}")
    public ResponseEntity<ApiResponse<List<TaskFindByTeacherIdWithClassDTO>>> getByTeacherIdWithClass(@PathVariable Integer id) {
        try {
            List<TaskFindByTeacherIdWithClassDTO> res = service.TaskFindByTeacherIdWithClass(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

//    由课程找作业
//    用在教师端(teacher/...)--已完成

    @GetMapping("byClassId/{id}")
    public ResponseEntity<ApiResponse<List<Task>>> getByClassId(@PathVariable Integer id) {
        try {
            List<Task> res = service.findByClassId(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    //    按课程ID查询作业统计信息（用在TeacherCourseManagement.vue和TeacherStudentManagement.vue）
    @GetMapping("byClassIdWithStats/{classId}")
    public ResponseEntity<ApiResponse<List<TaskFindByTeacherIdWithClassDTO>>> getByClassIdWithStats(@PathVariable Integer classId) {
        try {
            List<TaskFindByTeacherIdWithClassDTO> res = service.findByClassIdWithStats(classId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

//    用在学生端(/student/courses/{class_id}/{user_id})
    @GetMapping("byClassId/{class_id}/{user_id}")
    public ResponseEntity<ApiResponse<List<TaskWithNoteId>>> getByClassIdWithNoteId(@PathVariable Integer class_id, @PathVariable Integer user_id) {
        try {
            List<TaskWithNoteId> res = service.findByClassIdWithNoteId(class_id,user_id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

}
