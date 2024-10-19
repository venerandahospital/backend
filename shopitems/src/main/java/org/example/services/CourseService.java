package org.example.services;

import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.auth.services.UserAuthService;
import org.example.domains.Course;
import org.example.domains.ShopItem;
import org.example.domains.repositories.CourseRepository;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.CourseRequest;
import org.example.services.payloads.CourseUpdateRequest;
import org.example.services.payloads.ShopItemUpdateRequest;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CourseService {
    @Inject
    CourseRepository courseRepository;

    @Inject
    MySQLPool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    public Course createNewCourse(CourseRequest request){
        Course course = new Course();
        course.title = request.title;
        course.description = request.description;
        course.details = request.details;
        course.image = request.image;
        course.parent = request.parent;
        course.creationDate = LocalDate.now();


        courseRepository.persist(course);
        return course;
    }

    public List<Course> getAllCourses() {
        return courseRepository.listAll();
    }


    public Course updateCourseById(Long id, CourseUpdateRequest request) {
        return courseRepository.findByIdOptional(id)
                .map(course -> {

                    course.title = request.title;
                    course.description = request.description;
                    course.details = request.details;
                    course.image = request.image;
                    course.parent = request.parent;
                    course.creationDate = LocalDate.now();

                    courseRepository.persist(course);
                    return course;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }
}
