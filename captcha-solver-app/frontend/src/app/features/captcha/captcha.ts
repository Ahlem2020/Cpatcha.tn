import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { TokenStorageService } from '../auth/token-storage.service'; // For username and token

const API_URL = 'http://localhost:8080/api/captcha/'; // Adjust if backend is elsewhere
const WEBSOCKET_URL = 'http://localhost:8080/ws'; // WebSocket endpoint

@Injectable({
  providedIn: 'root'
})
export class CaptchaService {

  private stompClient: Client | null = null;
  private captchaUpdateSubject = new Subject<any>();
  public captchaUpdate$: Observable<any> = this.captchaUpdateSubject.asObservable();
  private subscription: StompSubscription | null = null;

  constructor(private http: HttpClient, private tokenStorage: TokenStorageService) { }

  public connectWebSocket(): void {
    if (this.stompClient && this.stompClient.active) {
      console.log('WebSocket already connected.');
      return;
    }

    const user = this.tokenStorage.getUser();
    if (!user || !user.username) {
      console.error('Cannot connect WebSocket: User not logged in or username not found.');
      return;
    }
    const username = user.username;
    const token = this.tokenStorage.getToken(); // For potential use in connect headers if needed

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      connectHeaders: {
        // Spring Security + STOMP often uses the existing HTTP session or a token passed here.
        // If your AuthTokenFilter processes JWT from headers for HTTP, it might also work for WebSocket upgrade requests
        // or you might need to pass the token explicitly if your server is configured for it.
        // For simplicity, we'll assume the session/JWT from HTTP carries over or client sends it.
        // If issues, uncomment and pass token:
        // Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to WebSocket:', frame);
      // Subscribe to user-specific CAPTCHA updates
      if (this.stompClient) { // Check if stompClient is not null
        this.subscription = this.stompClient.subscribe(`/user/${username}/queue/captcha-updates`, (message: Message) => {
          try {
            const update = JSON.parse(message.body);
            this.captchaUpdateSubject.next(update);
          } catch (e) {
            console.error('Error parsing WebSocket message body:', e, message.body);
          }
        });
        console.log(`Subscribed to /user/${username}/queue/captcha-updates`);
      }
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.onWebSocketError = (event) => {
        console.error('WebSocket error:', event);
    };

    this.stompClient.activate();
  }

  public disconnectWebSocket(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
      console.log('WebSocket disconnected.');
    }
    this.stompClient = null; // Clear client to allow reconnection
  }


  submitCaptcha(file: File): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    formData.append('image', file);

    const req = new HttpRequest('POST', API_URL + 'submit', formData, {
      reportProgress: true,
      responseType: 'json'
      // Note: For file uploads with Spring Security, ensure CSRF is handled if not disabled,
      // or tokens are correctly passed if using token-based auth.
      // If using session cookies, they should be sent automatically by the browser.
    });

    return this.http.request(req);
  }

  getSolution(id: number): Observable<any> {
    return this.http.get(API_URL + 'solution/' + id, { responseType: 'json' });
  }

  getHistory(): Observable<any> {
    return this.http.get(API_URL + 'history', { responseType: 'json' });
  }
}
