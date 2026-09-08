import { Routes } from '@angular/router';
import { LandingComponent } from './components/landing/landing.component';
import { LoginComponent } from './components/auth/login/login.component';
import { ForgotPasswordComponent } from './components/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/auth/reset-password/reset-password.component';
import { ActivateAccountComponent } from './components/auth/activate-account/activate-account.component';
import { CourseLearningComponent } from './components/course/pages/course-learning/course-learning.component';

export const routes: Routes = [
    {
        path: '',
        component: LandingComponent
    },
    {
        path: 'login',
        component: LoginComponent,
    },
    {
        path: 'forgot-password',
        component: ForgotPasswordComponent
    },
    {
        path: 'reset-password/:token',
        component: ResetPasswordComponent
    },
    {
        path: 'activate-account/:token',
        component: ActivateAccountComponent
    },
    {
        path: 'learning/:courseId',
        component: CourseLearningComponent
    },
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./components/dashboard/dashboard-layout/dashboard-layout.component')
                .then(m => m.DashboardLayoutComponent),
        children: [
            {
                path: '',
                loadComponent: () =>
                    import('./components/dashboard/dashboard-home/dashboard-home.component')
                        .then(m => m.DashboardHomeComponent)
            },
            {
                path: 'organizations',
                loadComponent: () =>
                    import('./components/organization/pages/organization-management/organization-management.component')
                        .then(m => m.OrganizationManagementComponent)
            },
            {
                path: 'organization/new',
                loadComponent: () =>
                    import('./components/organization/pages/organization-form/organization-form.component')
                        .then(m => m.OrganizationFormComponent)
            },
            {
                path: 'organization/:id/edit',
                loadComponent: () =>
                    import('./components/organization/pages/organization-form/organization-form.component')
                        .then(m => m.OrganizationFormComponent)
            },
            {
                path: 'departments',
                loadComponent: () =>
                    import('./components/department/pages/department-management/department-management.component')
                        .then(m => m.DepartmentManagementComponent)
            },
            {
                path: 'employees',
                loadComponent: () =>
                    import('./components/employee/pages/employee-management/employee-management.component')
                        .then(m => m.EmployeeManagementComponent)
            },
            {
                path: 'skills',
                loadComponent: () =>
                    import('./components/skill/pages/skill-management/skill-management.component')
                        .then(m => m.SkillManagementComponent)
            },
            {
                path: 'courses',
                loadComponent: () =>
                    import('./components/course/pages/course-management/course-management.component')
                        .then(m => m.CourseManagementComponent)
            },
            {
                path: 'course/create',
                loadComponent: () =>
                    import('./components/course/pages/create-course/create-course.component')
                        .then(m => m.CreateCourseComponent)
            },
            {
                path: 'course/:id',
                loadComponent: () =>
                    import('./components/course/pages/course-details/course-details.component')
                        .then(m => m.CourseDetailsComponent)
            },
            {
                path: 'course/:id/edit',
                loadComponent: () =>
                    import('./components/course/pages/create-course/create-course.component')
                        .then(m => m.CreateCourseComponent)
            },
            {
                path: 'course/:courseId/module/:moduleId/contents',
                loadComponent: () =>
                    import('./components/course/pages/module-content-management/module-content-management.component')
                        .then(m => m.ModuleContentManagementComponent)
            },
            {
                path: 'course/:courseId/module/:moduleId/content/create',
                loadComponent: () =>
                    import('./components/course/pages/create-module-content/create-module-content.component')
                        .then(m => m.CreateModuleContentComponent)
            },
            {
                path: 'course/:courseId/module/:moduleId/content/:contentId/edit',
                loadComponent: () =>
                    import('./components/course/pages/create-module-content/create-module-content.component')
                        .then(m => m.CreateModuleContentComponent)
            },
            {
                path: 'course/:courseId/module/:moduleId/content/:contentId/view',
                loadComponent: () =>
                    import('./components/course/pages/module-content-view/module-content-view.component')
                        .then(m => m.ModuleContentViewComponent)
            },
            {
                path: 'browse-courses',
                loadComponent: () =>
                    import('./components/course/pages/browse-courses/browse-courses.component')
                        .then(m => m.BrowseCoursesComponent)
            },
            {
                path: 'browse-course/:courseId',
                loadComponent: () =>
                    import('./components/course/pages/course-view/course-view.component')
                        .then(m => m.CourseViewComponent)
            },
            {
                path: 'my-learning',
                loadComponent: () =>
                    import('./components/course/pages/my-learning/my-learning.component')
                        .then(m => m.MyLearningComponent)
            },
            {
                path: 'achievements',
                loadComponent: () =>
                    import('./components/course/pages/employee-skills/employee-skills.component')
                        .then(m => m.EmployeeSkillsComponent)
            },
            {
                path: 'progress/:employeeId',
                loadComponent: () =>
                    import('./components/employee/pages/employee-progress-component/employee-progress-component.component')
                        .then(m => m.EmployeeProgressComponentComponent)
            },
            {
                path: 'course-requests',
                loadComponent: () =>
                    import('./components/course/pages/course-requests/course-requests.component')
                        .then(m => m.CourseRequestsComponent)
            },
            {
                path: 'overview',
                loadComponent: () =>
                    import('./components/dashboard/dashboard-home/dashboard-home.component')
                        .then(m => m.DashboardHomeComponent
                        )
            }
        ]
    }
]