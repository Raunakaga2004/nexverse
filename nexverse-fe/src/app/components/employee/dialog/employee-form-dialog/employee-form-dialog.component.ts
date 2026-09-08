import { Component, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { MatSelect } from '@angular/material/select';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { MATERIAL_IMPORTS } from '../../../../core/material/matrerial';
import { Department } from '../../../../core/models/department.model';
import { DepartmentService } from '../../../../core/services/department.service';
import { Employee } from '../../../../core/models/employee.model';
import { EmployeeService } from '../../../../core/services/employee.service';

@Component({
  selector: 'app-employee-form-dialog',
  imports: [MATERIAL_IMPORTS, ReactiveFormsModule, NgxMatSelectSearchModule],
  templateUrl: './employee-form-dialog.component.html',
  styleUrl: './employee-form-dialog.component.scss'
})
export class EmployeeFormDialogComponent implements OnInit, OnDestroy {
  @ViewChild(MatSelect)
  departmentSelect!: MatSelect;
  loading = false;
  loadingDepartments = false;
  readonly pageSize = 20;

  page = 1;
  last = false;
  searchKeyword = '';
  departments: Department[] = [];
  departmentSearchControl = new FormControl('', { nonNullable: true });
  private readonly destroy$ = new Subject<void>();

  form!: FormGroup<{
    firstName: FormControl<string>;
    lastName: FormControl<string | null>;
    email: FormControl<string>;
    phoneNumber: FormControl<string | null>;
    employeeCode: FormControl<string | null>;
    jobTitle: FormControl<string | null>;
    departmentName: FormControl<string>;
  }>;

  buildForm() {
    this.form = this.formBuilder.nonNullable.group({
      firstName: ['', Validators.required],
      lastName: this.formBuilder.control<string | null>(null),
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: this.formBuilder.control<string | null>(null),
      employeeCode: this.formBuilder.control<string | null>(null),
      jobTitle: this.formBuilder.control<string | null>(null),
      departmentName: ['', Validators.required]
    });
  }

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly departmentService: DepartmentService,
    private readonly dialogRef: MatDialogRef<EmployeeFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public readonly data: {
      mode: 'create' | 'edit';
      employee?: Employee;
    },
    private employeeService: EmployeeService
  ) { }

  ngOnInit(): void {
    this.buildForm();
    if (this.data.mode === 'edit' && this.data.employee) {
      this.form.patchValue({
        firstName: this.data.employee.firstName,
        lastName: this.data.employee.lastName,
        email: this.data.employee.email,
        phoneNumber: this.data.employee.phoneNumber,
        employeeCode: this.data.employee.employeeCode,
        jobTitle: this.data.employee.jobTitle,
        departmentName: this.data.employee.departmentName
      });
    }

    this.departmentSearchControl.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(keyword => {
        this.searchKeyword = keyword;
        this.loadDepartments(true);
      });
  }

  onDepartmentOpened(opened: boolean): void {
    if (!opened) {
      return;
    }
    if (this.departments.length === 0) {
      this.loadDepartments(true);
    }
    setTimeout(() => {
      const panel = this.departmentSelect.panel?.nativeElement;
      if (!panel) {
        return;
      }
      panel.removeEventListener('scroll', this.onDepartmentScroll);
      panel.addEventListener('scroll', this.onDepartmentScroll);
    });
  }

  private onDepartmentScroll = (event: Event): void => {
    const panel = event.target as HTMLElement;
    const atBottom = panel.scrollTop + panel.clientHeight >= panel.scrollHeight - 30;
    if (atBottom) {
      this.loadDepartments(false);
    }
  };

  loadDepartments(reset: boolean): void {
    if (this.loadingDepartments) {
      return;
    }
    if (reset) {
      this.page = 1;
      this.last = false;
      this.departments = [];
    }
    if (this.last) {
      return;
    }
    this.loadingDepartments = true;
    this.departmentService
      .getDepartments(
        this.page,
        this.pageSize,
        'createdAt',
        'desc',
        this.searchKeyword,
        true
      )
      .subscribe({
        next: response => {
          this.departments.push(...response.data!.content);
          this.page++;
          this.last = response.data!.last;
          this.loadingDepartments = false;
        },
        error: () => {
          this.loadingDepartments = false;
        }
      });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {
    this.loading = true
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = {
      firstName: this.form.value.firstName,
      lastName: this.form.value.lastName === '' ? null : this.form.value.lastName,
      email: this.form.value.email,
      phoneNumber: this.form.value.phoneNumber === '' ? null : this.form.value.phoneNumber,
      employeeCode: this.form.value.employeeCode === '' ? null : this.form.value.employeeCode,
      jobTitle: this.form.value.jobTitle === '' ? null : this.form.value.jobTitle,
      departmentName: this.form.value.departmentName,
    };
    const formData = new FormData();
    formData.append(
      'employee',
      new Blob(
        [JSON.stringify(request)],
        {
          type: 'application/json'
        }
      )
    );

    if (this.data.mode === 'create') {
      this.employeeService.createEmployee(formData).subscribe({
        next: () => {
          this.dialogRef.close(formData);
          this.loading = false
        },
        error: () => this.loading = false
      })
    }
    else {
      this.employeeService.editEmployee(this.data.employee!.id, formData).subscribe({
        next: () => {
          this.dialogRef.close(formData);
          this.loading = false
        },
        error: () => this.loading = false
      })
    }

  }

  get title(): string {
    return this.data.mode === 'create'
      ? 'Create Employee'
      : 'Edit Employee';
  }

  get actionLabel(): string {
    return this.data.mode === 'create'
      ? 'Create'
      : 'Save Changes';
  }

  ngOnDestroy(): void {
    this.departmentSelect?.panel?.nativeElement
      ?.removeEventListener('scroll', this.onDepartmentScroll);
    this.destroy$.next();
    this.destroy$.complete();
  }
}