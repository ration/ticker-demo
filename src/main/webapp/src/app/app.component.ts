import {Component} from '@angular/core';
import {News} from './news.model';
import {TickerService} from './ticker-service';
import {Subscription} from 'rxjs';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  expanded = false;
  private subscription: Subscription = null;
  news: News = {id: 3, text: 'this is some longer text', description: 'News item', priority: 1};
  stop_button = "stop";

  constructor(private tickerService: TickerService) {
  }


  public stop() {
    this.tickerService.pause();
    this.expanded = !this.expanded;
  }


  public subscribe() {
    this.subscription = this.tickerService.ticker().subscribe(data => {
      this.news = data;
    });
  }

  public connect() {
    if (this.subscription == null) {
      this.tickerService.initSocket();
      this.subscribe();
    }
  }


  public getPriority() {
    return this.news.priority < 2 ? 'alert-danger' : 'alert-primary';
  }

}
