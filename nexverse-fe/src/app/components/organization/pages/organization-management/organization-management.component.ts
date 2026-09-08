import { Component, OnInit } from '@angular/core';
import { DatePipe, LowerCasePipe, NgClass, TitleCasePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { Router, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Organization } from '../../../../core/models/organizations.model';
import { OrganizationStatus } from '../../../../core/enums/organization-status.enum';
import { OrganizationService } from '../../../../core/services/organization.service';
import { OrganizationViewDialogComponent } from '../../dialog/organization-view-dialog/organization-view-dialog.component';
import { ConfirmationDialogComponent } from '../../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { PageResponse } from '../../../../core/models/page-response.model';
import { ApiResponse } from '../../../../core/models/api-response.model';
import { ArchiveStatus } from '../../../../core/enums/archive-status.enum';
import { Sort } from '@angular/material/sort';
import { NoDataComponent } from "../../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-organization-management',
  imports: [MATERIAL_IMPORTS, NgClass, ReactiveFormsModule, TitleCasePipe, DatePipe, RouterLink, NoDataComponent, LowerCasePipe],
  templateUrl: './organization-management.component.html',
  styleUrl: './organization-management.component.scss'
})
export class OrganizationManagementComponent implements OnInit {
  loading = false;
  organizations: Organization[] = [];
  totalElements = 0;
  displayedColumns: string[] = [
    'name',
    'status',
    'email',
    'phone',
    'country',
    'createdAt',
    'updatedAt',
    'archiveStatus',
    'actions'
  ];
  searchControl = new FormControl('', {
    nonNullable: true
  })
  statusControl = new FormControl<OrganizationStatus | "ALL">("ALL");
  enabledControl = new FormControl<ArchiveStatus>(ArchiveStatus.ALL);
  query = {
    page: 1,
    size: 10,
    search: '',
    status: "ALL" as OrganizationStatus | "ALL",
    enabled: "ALL" as ArchiveStatus,
    sort: 'createdAt',
    direction: 'DESC' as 'ASC' | 'DESC'
  };
  statuses = [
    "ALL",
    "ACTIVE",
    "PENDING",
    "SUSPENDED"
  ]
  archiveStatuses = [
    "ALL",
    "ARCHIVED",
    "NON-ARCHIVED",
  ]
  constructor(private organizationService: OrganizationService, private router: Router, private dialog: MatDialog) { }
  ngOnInit(): void {
    this.initializeSearch();
    this.initializeFilters();
    this.loadOrganizations();
  }
  viewOrganization(id: number) {
    this.organizationService.getOrganization(id).subscribe(
      response => {
        this.dialog.open(OrganizationViewDialogComponent, {
          minWidth: '1000px',
          minHeight: '650px',
          data: response.data,
          panelClass: 'dialog-box'
        })
      }
    );
  }
  editOrganization(id: number) {
    this.router.navigateByUrl(`/dashboard/organization/${id}/edit`)
  }
  disableOrganization(organization: Organization) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Archive Organization',
        message: `Are you sure you want to archive organization ${organization.name}?`,
        confirmText: 'Archive',
        cancelText: 'Close',
        confirmColor: 'warn'
      },
      panelClass: 'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.organizationService.disableOrganization(organization.id).subscribe(() => {
        this.loadOrganizations();
      })
    })
  }
  enableOrganization(organization: Organization) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Unarchive Organization',
        message: `Are you sure you want to unarchive organization ${organization.name}?`,
        confirmText: 'Unarchive',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: 'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.organizationService.enableOrganization(organization.id).subscribe(() => {
        this.loadOrganizations();
      })
    })
  }
  suspendOrganization(organization: Organization) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Suspend Organization',
        message: `Are you sure you want to suspend organization ${organization.name}?`,
        confirmText: 'Suspend',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: 'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.organizationService.suspendOrganization(organization.id).subscribe(() => {
        this.loadOrganizations();
      })
    })
  }

  unsuspendOrganization(organization: Organization) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: true,
      data: {
        title: 'Reactivate Organization',
        message: `Are you sure you want to reactivate organization ${organization.name}?`,
        confirmText: 'Reactivate',
        cancelText: 'Close',
        confirmColor: 'primary'
      },
      panelClass: 'dialog-box'
    })

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;
      this.organizationService.unsuspendOrganization(organization.id).subscribe(() => {
        this.loadOrganizations();
      })
    })
  }
  private initializeSearch(): void {
    this.searchControl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(value => {
      this.query.search = value.trim();
      this.query.page = 1;
      this.loadOrganizations();
    })
  }
  private initializeFilters() {
    this.statusControl.valueChanges.subscribe(status => {
      this.query.status = status!; // not null
      this.query.page = 1;
      this.loadOrganizations();
    });
    this.enabledControl.valueChanges.subscribe(enabled => {
      this.query.enabled = enabled!; // enabled will not be null
      this.query.page = 1;
      this.loadOrganizations();
    })
  }
  pageChanged(event: PageEvent): void {
    this.query.page = event.pageIndex + 1;
    this.query.size = event.pageSize;
    this.loadOrganizations();
  }
  sort(field: string): void {
    if (this.query.sort === field) {
      this.query.direction = this.query.direction === 'ASC' ? 'ASC' : 'DESC';
    }
    else {
      this.query.sort = field;
      this.query.direction = 'ASC';
    }
    console.log(this.query)
    this.loadOrganizations();
  }
  private loadOrganizations() {
    this.loading = true;
    const enabled = this.query.enabled === "ARCHIVED" ? false : this.query.enabled === "NON-ARCHIVED" ? true : '';
    this.organizationService.getOrganizations(
      this.query.page,
      this.query.size,
      this.query.sort,
      this.query.direction,
      this.query.search,
      enabled,
      this.query.status || undefined
    ).subscribe({
      next: (response: ApiResponse<PageResponse<Organization>>) => {
        console.log(response);
        this.organizations = response.data!.content as Organization[];
        this.totalElements = response.data!.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
  isSortable(column: string): boolean {
    return [
      'name',
      'email',
      'createdAt',
      'updatedAt'
    ].includes(column);
  }

  sortChanged(sort: Sort): void {
    console.log(sort)
    this.query.sort = sort.active;
    this.query.direction = (sort.direction.toUpperCase() as 'ASC'|'DESC');
    console.log(this.query)
    this.loadOrganizations();
  }
}