import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.html',
  styleUrls: ['./subscription.css'],
  standalone: true
})
export class SubscriptionComponent implements OnInit {
  subscriptionPlans: any;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.http.get('http://localhost:8080/api/subscription-plans').subscribe(data => {
      this.subscriptionPlans = data;
    });
  }
}
