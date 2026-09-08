import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { FileSizePipe } from '../../pipes/file-size.pipe';

@Component({
  selector: 'app-file-upload',
  imports: [MATERIAL_IMPORTS, FileSizePipe],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.scss'
})
export class FileUploadComponent implements OnChanges {
  ngOnChanges(changes: SimpleChanges): void {
    if(changes['file']){
      this.updatePreview();
    }
  }

  @Input()
  accept = '*/*';

  @Input()
  label = 'Drag & drop your file here';

  @Input()
  hint = 'or click to browse';

  @Output()
  fileSelected = new EventEmitter<File | null>();

  @Input()
  file: File | null = null;

  isDragging = false;
  imagePreview: string | null = null;

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
  }

  private updatePreview(): void {
    if (this.file && this.file.type.startsWith('image/')) {
      const reader = new FileReader();

      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };

      reader.readAsDataURL(this.file);
    } else {
      this.imagePreview = null;
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    if (!event.dataTransfer?.files.length) {
      return;
    }
    this.setFile(event.dataTransfer.files[0]);
  }

  browse(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    this.setFile(input.files[0]);
    input.value = '';
  }

  removeFile(): void {
    this.file = null;
    this.imagePreview = null;
    this.fileSelected.emit(null);
  }

  private setFile(file: File): void {
    this.file = file;
    this.updatePreview();
    this.fileSelected.emit(file);
  }

  get fileSize(): number {
    if (!this.file) {
      return 0;
    }
    return this.file.size;
  }
}
