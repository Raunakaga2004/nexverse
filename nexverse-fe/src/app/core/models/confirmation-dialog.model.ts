export interface ConfirmationDialogData {
    title : string;
    message : string;
    confirmText : string;
    confirmColor? : 'primary' | 'accent' | 'warn';
    cancelText : string;
}