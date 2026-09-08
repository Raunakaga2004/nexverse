import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe, LowerCasePipe, NgClass, TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Employee } from '../../../../core/models/employee.model';
import { Department } from '../../../../core/models/department.model';
import { UserStatus } from '../../../../core/enums/user-status.enum';
import { EmployeeService } from '../../../../core/services/employee.service';
import { EmployeeFormDialogComponent } from '../../dialog/employee-form-dialog/employee-form-dialog.component';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { ImportEmployeesDialogComponent } from '../../dialog/import-employees-dialog/import-employees-dialog.component';
import { ViewEmployeeDialogComponent } from '../../dialog/view-employee-dialog/view-employee-dialog.component';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { ArchiveStatus } from '../../../../core/enums/archive-status.enum';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-employee-management',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, TitleCasePipe, DatePipe, NgClass, NoDataComponent, LowerCasePipe],
  templateUrl: './employee-management.component.html',
  styleUrl: './employee-management.component.scss'
})
export class EmployeeManagementComponent implements OnInit {
  loading = false;
  currentUserRole: string | undefined;
  employees: Employee[] = [];
  totalElements = 0;
  displayedColumns: string[] = [
    'name',
    'role',
    'status',
    'email',
    'departmentName',
    'createdAt',
    'updatedAt',
    'archiveStatus',
    'actions'
  ];
  archiveStatuses = [
    "ALL",
    "ARCHIVED",
    "NON-ARCHIVED",
  ];
  roles = [
    "ALL",
    "EMPLOYEE",
    "MANAGER"
  ]
  statuses = [
    "ALL",
    "ACTIVE",
    "PENDING",
    "SUSPENDED"
  ];
  departments: Department[] = [];
  searchControl = new FormControl('', {
    nonNullable: true
  })
  statusControl = new FormControl<UserStatus | "ALL">("ALL");
  roleControl = new FormControl<"EMPLOYEE" | "MANAGER" | "ALL">("ALL");
  enabledControl = new FormControl<ArchiveStatus>(ArchiveStatus.ALL);

  query = {
    page: 1,
    size: 10,
    search: '',
    enabled: "ALL" as ArchiveStatus,
    status: "ALL" as UserStatus | "ALL",
    role: "ALL" as "EMPLOYEE" | "MANAGER" | "ALL",
    sort: 'createdAt',
    direction: 'desc' as 'asc' | 'desc'
  };
  constructor(private employeeService: EmployeeService, private router: Router, private dialog: MatDialog, private currentUserService: CurrentUserService) { }
  ngOnInit(): void {
    this.currentUserRole = this.currentUserService.getCurrentUserRole();
    this.initializeSearch();
    this.initializeFilters();
    this.loadEmployees();
  }

  createEmployee() {
    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '750px',
      disableClose: true,
      data: {
        mode: 'create',
        departments: this.departments
      },
      panelClass: "dialog-box"
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.loadEmployees();
    })
  }

  editEmployee(employee: Employee) {
    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '750px',
      disableClose: true,
      data: {
        mode: 'edit',
        employee,
        departments: this.departments
      },
      panelClass: "dialog-box"
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.loadEmployees();
    })
  }
  disableEmployee(employee: Employee) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Archive Employee',
        message: `Are you sure you want to archive employee ${employee.firstName + (employee.lastName? ` ${employee.lastName}` : "")}?`,
        confirmText: 'Archive',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.employeeService.disableEmployee(employee.id).subscribe(() => {
        this.loadEmployees();
      })
    })
  }
  enableEmployee(employee: Employee) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Unarchive Employee',
        message: `Are you sure you want to unarchive employee ${employee.firstName + (employee.lastName? ` ${employee.lastName}` : "")}?`,
        confirmText: 'Unarchive',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.employeeService.enableEmployee(employee.id).subscribe(() => {
        this.loadEmployees();
      })
    })
  }

  viewEmployee(id: number) {
    this.employeeService.getEmployee(id).subscribe(
      response => {
        this.dialog.open(ViewEmployeeDialogComponent, {
          minWidth: '800px',
          data: response.data,
          panelClass: "dialog-box"
        })
      }
    );
  }

  suspendEmployee(employee: Employee) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Suspend Employee',
        message: `Are you sure you want to suspend organization ${employee.firstName} ${employee.lastName}?`,
        confirmText: 'Suspend',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.employeeService.suspendEmployee(employee.id).subscribe(() => {
        this.loadEmployees();
      })
    })
  }

  unsuspendEmployee(employee: Employee) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Reactivate Employee',
        message: `Are you sure you want to reactivate employee ${employee.firstName} ${employee.lastName}?`,
        confirmText: 'Reactivate',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.employeeService.unsuspendEmployee(employee.id).subscribe(() => {
        this.loadEmployees();
      })
    })
  }
  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.query.search = value.trim();
      this.query.page = 1;
      this.loadEmployees();
    })
  }
  private initializeFilters(): void {
    this.enabledControl.valueChanges.subscribe(enabled => {
      this.query.enabled = enabled!;
      this.query.page = 1;
      this.loadEmployees();
    });

    this.statusControl.valueChanges.subscribe(status => {
      this.query.status = status!;
      this.query.page = 1;
      this.loadEmployees();
    });

    this.roleControl.valueChanges.subscribe(role => {
      this.query.role = role!;
      this.query.page = 1;
      this.loadEmployees();
    });
  }
  pageChanged(event: PageEvent): void {
    this.query.page = event.pageIndex + 1;
    this.query.size = event.pageSize;
    this.loadEmployees();
  }
  sort(field: string): void {
    if (this.query.sort === field) {
      this.query.direction = this.query.direction === 'asc' ? 'desc' : 'asc';
    }
    else {
      this.query.sort = field;
      this.query.direction = 'asc';
    }
    this.loadEmployees();
  }
  private loadEmployees() {
    this.loading = true;
     const enabled = this.query.enabled === "ARCHIVED" ? false : this.query.enabled === "NON-ARCHIVED" ? true : '';
    this.employeeService.getEmployees(
      this.query.page,
      this.query.size,
      this.query.sort,
      this.query.direction,
      this.query.role,
      this.query.search,
      enabled,
      this.query.status || undefined,
    ).subscribe({
      next: (response: ApiResponse<PageResponse<Employee>>) => {
        this.employees = response.data!.content as Employee[];
        this.totalElements = response.data!.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  bulkOnboardEmployees() {
    const dialogRef = this.dialog.open(ImportEmployeesDialogComponent, {
      panelClass: "dialog-box"
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.loadEmployees()
    })
  }

  viewLearningProgress(employeeId: number) {
    this.router.navigate(['/dashboard/progress', employeeId]);
  }
}