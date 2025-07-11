import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common'; // For *ngIf, *ngFor etc.
import { FormsModule } from '@angular/forms'; // For potential forms
import { Subscription } from 'rxjs';
import { CaptchaService } from '../captcha.service'; // Corrected path to captcha.service.ts or captcha.ts
import { TokenStorageService } from '../../auth/token-storage.service'; // For user info

@Component({
  selector: 'app-captcha-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule], // Add CommonModule, FormsModule
  templateUrl: './captcha-dashboard.html',
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HttpEventType, HttpResponse } from '@angular/common/http';
import { CaptchaService } from '../captcha.service';
import { TokenStorageService } from '../../auth/token-storage.service';

interface CaptchaDisplayItem {
  id: number;
  originalFileName?: string;
  status?: string;
  solution?: string;
  submittedAt?: Date;
  solvedAt?: Date;
  imageDataUrl?: string; // For previewing submitted image or if fetched
  progress?: number; // For upload progress
  message?: string; // For status messages
}

@Component({
  selector: 'app-captcha-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './captcha-dashboard.html',
  styleUrls: ['./captcha-dashboard.css'] // Corrected to styleUrls
})
export class CaptchaDashboardComponent implements OnInit, OnDestroy {

  private wsSubscription: Subscription | null = null;

  // For new submissions
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  uploadProgress = 0;
  submissionMessage = '';

  // For displaying submissions (history and real-time updates)
  captchaSubmissions: CaptchaDisplayItem[] = [];

  constructor(
    private captchaService: CaptchaService,
    private tokenStorageService: TokenStorageService
  ) {}

  ngOnInit(): void {
    if (this.tokenStorageService.isLoggedIn()) {
      this.captchaService.connectWebSocket();
      this.loadHistory();

      this.wsSubscription = this.captchaService.captchaUpdate$.subscribe({
        next: (update: CaptchaDisplayItem) => {
          console.log('Received CAPTCHA update in component:', update);
          this.handleCaptchaUpdate(update);
        },
        error: (err) => {
          console.error('Error in WebSocket subscription:', err);
        }
      });
    } else {
      console.warn('User not logged in. CAPTCHA Dashboard functionality might be limited.');
      // Redirect to login or show appropriate message
    }
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
    this.captchaService.disconnectWebSocket();
  }

  onFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList[0]) {
      this.selectedFile = fileList[0];
      this.submissionMessage = '';
      this.uploadProgress = 0;

      const reader = new FileReader();
      reader.onload = (e) => this.imagePreview = reader.result;
      reader.readAsDataURL(this.selectedFile);
    } else {
      this.selectedFile = null;
      this.imagePreview = null;
    }
  }

  submitCaptcha(): void {
    if (!this.selectedFile) {
      this.submissionMessage = 'Please select an image file.';
      return;
    }

    this.uploadProgress = 0;
    this.submissionMessage = 'Submitting CAPTCHA...';

    this.captchaService.submitCaptcha(this.selectedFile).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress) {
          this.uploadProgress = Math.round(100 * event.loaded / event.total);
        } else if (event instanceof HttpResponse) {
          this.submissionMessage = event.body.message || 'CAPTCHA submitted successfully!';
          // Add to list with pending status, WebSocket update will provide final status
          const newSubmission: CaptchaDisplayItem = {
            id: event.body.submissionId,
            originalFileName: this.selectedFile?.name,
            status: event.body.status || 'PENDING',
            submittedAt: new Date(),
            imageDataUrl: this.imagePreview as string // Show preview
          };
          this.addOrUpdateSubmissionInList(newSubmission);
          this.selectedFile = null; // Clear selection
          this.imagePreview = null; // Clear preview
          this.uploadProgress = 0;
        }
      },
      error: (err: any) => {
        this.uploadProgress = 0;
        this.submissionMessage = err.error?.message || err.message || 'CAPTCHA submission failed.';
        console.error(err);
      }
    });
  }

  loadHistory(): void {
    this.captchaService.getHistory().subscribe({
      next: (history: CaptchaDisplayItem[]) => {
        // Convert date strings to Date objects if necessary
        this.captchaSubmissions = history.map(item => ({
            ...item,
            submittedAt: item.submittedAt ? new Date(item.submittedAt) : undefined,
            solvedAt: item.solvedAt ? new Date(item.solvedAt) : undefined,
        })).sort((a, b) => (b.submittedAt?.getTime() || 0) - (a.submittedAt?.getTime() || 0) ); // Sort by newest first
      },
      error: (err) => {
        console.error('Failed to load CAPTCHA history:', err);
        // Display error to user
      }
    });
  }

  private handleCaptchaUpdate(update: CaptchaDisplayItem): void {
    this.addOrUpdateSubmissionInList(update);
  }

  private addOrUpdateSubmissionInList(item: CaptchaDisplayItem): void {
    const index = this.captchaSubmissions.findIndex(s => s.id === item.id);
    if (index > -1) {
      // Update existing item, preserving preview if item doesn't have imageDataUrl
      const existingItem = this.captchaSubmissions[index];
      this.captchaSubmissions[index] = {
        ...existingItem, // Keep potential existing imageDataUrl if not in update
        ...item, // Overwrite with new data
        submittedAt: item.submittedAt ? new Date(item.submittedAt) : existingItem.submittedAt,
        solvedAt: item.solvedAt ? new Date(item.solvedAt) : existingItem.solvedAt,
      };
    } else {
      this.captchaSubmissions.unshift({ // Add to the beginning of the array
          ...item,
          submittedAt: item.submittedAt ? new Date(item.submittedAt) : undefined,
          solvedAt: item.solvedAt ? new Date(item.solvedAt) : undefined,
      });
    }
    // Re-sort after update/add to ensure order by submission date (newest first)
    this.captchaSubmissions.sort((a, b) => (b.submittedAt?.getTime() || 0) - (a.submittedAt?.getTime() || 0) );
  }

  refreshSolution(submissionId: number): void {
    this.captchaService.getSolution(submissionId).subscribe({
        next: (solution: CaptchaDisplayItem) => {
            this.handleCaptchaUpdate(solution);
        },
        error: (err) => {
            console.error(`Failed to refresh solution for ${submissionId}:`, err);
            // Update item with error message
            const item = this.captchaSubmissions.find(s => s.id === submissionId);
            if (item) {
                item.message = err.error?.message || 'Failed to refresh solution.';
            }
        }
    });
  }
}
