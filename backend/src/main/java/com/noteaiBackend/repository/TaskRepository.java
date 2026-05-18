package com.noteaiBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.noteaiBackend.dto.ClassFindByTeacherIdDTO;
import com.noteaiBackend.dto.TaskFindByTeacherIdWithClassDTO;
import com.noteaiBackend.dto.TaskWithNoteId;
import com.noteaiBackend.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query(value = "SELECT t.*, c.class_name FROM task t JOIN class c ON t.class_id = c.id WHERE c.teacher_id = :id;",nativeQuery = true)
    List<ClassFindByTeacherIdDTO> findByTeacherId(@Param("id")Integer id);

    @Query(value = """
    SELECT
        c.id AS classId,
        task.id AS taskId,
        c.class_name AS className,
        task.title AS title,
        COUNT(DISTINCT note.id) AS noteCount,
        task.deadline AS deadline,
        cc.join_count AS studentCount
    FROM class c
    LEFT JOIN task ON c.id = task.class_id
    LEFT JOIN note ON task.id = note.task_id
    LEFT JOIN (
        SELECT class_id, COUNT(*) AS join_count
        FROM class_joined
        GROUP BY class_id
    ) cc ON c.id = cc.class_id
    WHERE c.id = :classId
    GROUP BY c.id, task.id, c.class_name, task.title, task.deadline, cc.join_count
    ORDER BY
        CASE WHEN task.deadline IS NULL THEN 1 ELSE 0 END,
        task.deadline DESC
""", nativeQuery = true)
    List<TaskFindByTeacherIdWithClassDTO> findByClassIdWithStats(@Param("classId") Integer classId);

    @Query(value = """
    SELECT
        c.id AS classId,
        task.id AS taskId,
        c.class_name AS className,
        task.title AS title,
        COUNT(DISTINCT note.id) AS noteCount,
        task.deadline AS deadline,
        cc.join_count AS studentCount
    FROM class c
    LEFT JOIN task ON c.id = task.class_id
    LEFT JOIN note ON task.id = note.task_id
    LEFT JOIN (
        SELECT class_id, COUNT(*) AS join_count
        FROM class_joined
        GROUP BY class_id
    ) cc ON c.id = cc.class_id
    WHERE c.teacher_id = :id
    GROUP BY c.id, task.id, c.class_name, task.title, task.deadline, cc.join_count
    ORDER BY
        CASE WHEN task.deadline IS NULL THEN 1 ELSE 0 END,
        task.deadline DESC
""", nativeQuery = true)
    List<TaskFindByTeacherIdWithClassDTO> findByTeacherIdWithClass(@Param("id") Integer id);

    List<Task> findByClassId(Integer classId);

    @Query(value = """
    SELECT 
        task.id,
        task.title as task_title,
        task.deadline,
        note.id as note_id,
        note.is_score
    FROM task
    LEFT JOIN note ON note.task_id = task.id AND note.user_id = :user_id
    WHERE task.class_id = :class_id
    ORDER BY task.deadline
""", nativeQuery = true)
    List<TaskWithNoteId> findByClassIdWithNoteId(
            @Param("class_id") Integer classId,
            @Param("user_id") Integer userId
    );

    @Query(value = """
        SELECT t.*, c.class_name as course_name
        FROM task t
        JOIN class c ON t.class_id = c.id
        JOIN class_joined cj ON c.id = cj.class_id
        LEFT JOIN note n ON t.id = n.task_id AND n.user_id = :student_id
        WHERE cj.student_id = :student_id AND n.id IS NULL
        ORDER BY t.deadline ASC
""", nativeQuery = true)
    List<Object[]> findPendingAssignmentsByStudentId(@Param("student_id") Integer studentId);

    @Query(value = "SELECT COUNT(*) FROM task WHERE class_id = :classId", nativeQuery = true)
    long countByClassId(@Param("classId") Integer classId);
}
