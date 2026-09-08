import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Department } from '../../../../core/models/department.model';
import { DepartmentService } from '../../../../core/services/department.service';
import { DepartmentViewDialogComponent } from '../../dialog/department-view-dialog/department-view-dialog.component';
import { DepartmentCreateDialogComponent } from '../../dialog/department-create-dialog/department-create-dialog.component';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ChangeDepartmentManagerDialogComponent } from '../../dialog/change-department-manager-dialog/change-department-manager-dialog.component';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { ArchiveStatus } from '../../../../core/enums/archive-status.enum';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-department-management',
  imports: [ReactiveFormsModule, MATERIAL_IMPORTS, TitleCasePipe, DatePipe, NoDataComponent],
  templateUrl: './department-management.component.html',
  styleUrl: './department-management.component.scss'
})
export class DepartmentManagementComponent implements OnInit {
  loading = false;
  departments: Department[] = [];
  totalElements = 0;
  displayedColumns: string[] = [
    'name',
    'departmentManager',
    'employeeCount',
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
  searchControl = new FormControl('', {
    nonNullable: true
  })
  enabledControl = new FormControl<ArchiveStatus>(ArchiveStatus.ALL);
  query = {
    page: 1,
    size: 10,
    search: '',
    enabled: "ALL" as ArchiveStatus,
    sort: 'createdAt',
    direction: 'desc' as 'asc' | 'desc'
  };
  constructor(private departmentService: DepartmentService, private router: Router, private dialog: MatDialog) { }
  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadDepartments();
  }
  viewDepartment(id: number) {
    this.departmentService.getDepartment(id).subscribe(
      response => {
        this.dialog.open(DepartmentViewDialogComponent, {
          width: '600px',
          data: response.data,
          panelClass: "dialog-box"
        })
      }
    );
  }
  editDepartment(department: Department) {
    const dialogRef = this.dialog.open(DepartmentCreateDialogComponent, {
      disableClose: true,
      data: {
        mode: 'edit',
        department: {
          id: department.id,
          name: department.name
        }
      },
      panelClass: "dialog-box"
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;

      this.departmentService.editDepartment(
        department.id,
        result
      ).subscribe(() => {
        this.loadDepartments();
      });
    });
  }
  disableDepartment(department: Department) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Archive Department',
        message: `Are you sure you want to archive department ${department.name}?`,
        confirmText: 'Archive',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.departmentService.disableDepartment(department.id).subscribe(() => {
        this.loadDepartments();
      })
    })
  }
  createDepartment() {
    const dialogRef = this.dialog.open(DepartmentCreateDialogComponent, {
      disableClose: true,
      data: {
        mode: 'create'
      },
      panelClass: "dialog-box"
    });
    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.departmentService.createDepartment(result).subscribe(() => {
        this.loadDepartments();
      });
    });
  }
  enableDepartment(department: Department) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Unarchive Department',
        message: `Are you sure you want to unarchive department ${department.name}?`,
        confirmText: 'Unarchive',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: "dialog-box"
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.departmentService.enableDepartment(department.id).subscribe(() => {
        this.loadDepartments();
      })
    })
  }
  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.query.search = value.trim();
      this.query.page = 1;
      this.loadDepartments();
    })
  }
  private initializeFilters() {
    this.enabledControl.valueChanges.subscribe(enabled => {
      this.query.enabled = enabled!;
      this.query.page = 1;
      this.loadDepartments();
    })
  }

  changeManager(department: Department): void {
    const dialogRef = this.dialog.open(
      ChangeDepartmentManagerDialogComponent,
      {
        width: '600px',
        data: {
          departmentName: department.name
        },
        panelClass: "dialog-box"
      }
    );

    dialogRef.afterClosed().subscribe(selectedEmployeeId => {
      if (selectedEmployeeId) {
        this.departmentService.assignManager(
          department.id,
          selectedEmployeeId
        ).subscribe(response => {
          this.loadDepartments();
        })
      }
    });
  }
  pageChanged(event: PageEvent): void {
    this.query.page = event.pageIndex + 1;
    this.query.size = event.pageSize;
    this.loadDepartments();
  }
  sort(field: string): void {
    if (this.query.sort === field) {
      this.query.direction = this.query.direction === 'asc' ? 'desc' : 'asc';
    }
    else {
      this.query.sort = field;
      this.query.direction = 'asc';
    }
    this.loadDepartments();
  }
  private loadDepartments() {
    this.loading = true;
    const enabled = this.query.enabled === "ARCHIVED" ? false : this.query.enabled === "NON-ARCHIVED" ? true : '';
    this.departmentService.getDepartments(
      this.query.page,
      this.query.size,
      this.query.sort,
      this.query.direction,
      this.query.search,
      enabled
    ).subscribe({
      next: (response: ApiResponse<PageResponse<Department>>) => {
        this.departments = response.data!.content as Department[];
        this.totalElements = response.data!.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
