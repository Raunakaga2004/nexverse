import { Injectable } from '@angular/core';
import { SidebarItem } from '../models/sidebar-item.model';
import { CurrentUserService } from './current-user.service';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  role!: string | undefined;
  constructor(private currentUserService: CurrentUserService) { }
  getSidebarItems(): SidebarItem[] {
    this.currentUserService.currentUser$.subscribe({
      next: (currentUser) => {
        this.role = currentUser?.role;
      }
    })
    switch (this.role) {
      case 'SUPER_ADMIN':
        return this.getSuperAdminItems();
      case 'ADMIN':
        return this.getOrgAdminItems();
      case 'MANAGER':
        return this.getManagerItems();
      case 'EMPLOYEE':
        return this.getEmployeeItems();
      default:
        return [];
    }
  }

  private getSuperAdminItems(): SidebarItem[] {
    return [
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/dashboard'
      },
      {
        label: 'Organizations',
        icon: 'apartment',
        route: '/dashboard/organizations'
      }
    ]
  }

  private getOrgAdminItems(): SidebarItem[] {
    return [
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/dashboard'
      },
      {
        label: 'Departments',
        icon: 'account_tree',
        route: '/dashboard/departments'
      },
      {
        label: 'Employees',
        icon: 'groups',
        route: '/dashboard/employees'
      },
      {
        label: 'Skills',
        icon: 'workspace_premium',
        route: '/dashboard/skills'
      },
      {
        label: 'Courses',
        icon: 'school',
        route: '/dashboard/courses'
      },
    ]
  }

  private getManagerItems(): SidebarItem[] {
    return [
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/dashboard'
      },
      {
        label: 'Employees',
        icon: 'groups',
        route: '/dashboard/employees'
      },
      {
        label: 'Courses',
        icon: 'school',
        route: '/dashboard/courses'
      },
      {
        label: 'Pending Requests',
        icon: 'pending_actions',
        route: '/dashboard/course-requests'
      },
    ]
  }

  private getEmployeeItems(): SidebarItem[] {
    return [
      {
        label: 'Overview',
        icon: 'dashboard',
        route: '/dashboard'
      },
      {
        label: 'Browse Course',
        icon: 'explore',
        route: '/dashboard/browse-courses'
      },
      {
        label: 'My Learning',
        icon: 'school',
        route: '/dashboard/my-learning'
      },
      {
        label: 'Achievements',
        icon: 'emoji_events',
        route: '/dashboard/achievements'
      },
    ]
  }

  public getPersonalManagerItems(): SidebarItem[] {
    return [
      {
        label: 'Overview',
        icon: 'dashboard',
        route: '/dashboard/overview'
      },
      {
        label: 'My Learning',
        icon: 'school',
        route: '/dashboard/my-learning'
      },
      {
        label: 'Browse Course',
        icon: 'explore',
        route: '/dashboard/browse-courses'
      },
      {
        label: 'Achievements',
        icon: 'emoji_events',
        route: '/dashboard/achievements'
      },
    ]
  }
}