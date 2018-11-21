import {Injectable} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {JsonSerializers, RSocketClient, MAX_STREAM_ID} from 'rsocket-core';
import {News} from './news.model';
import {Observable, Subject, Subscription} from 'rxjs';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class TickerService {
  private url = "http://localhost:8080";

  private host = 'localhost';
  private port = 9988;
  private subject = new Subject<News>();
  private previous: News = null;
  private subscription = null;
  readonly SINGLE_REQ = 1;

  private counter = this.SINGLE_REQ;
  private paused = false;

  constructor(private http: HttpClient) {
  }

  public ticker(): Observable<News> {
    return this.subject.asObservable();
  }

  public initSocket() {
    const client = new RSocketClient({
      // send/receive objects instead of strings/buffers
      serializers: JsonSerializers,
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: 'application/json',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://' + this.host + ':' + this.port,
        wsCreator: url => {
          return new WebSocket(url);
        }
      })
    });
    const self = this;
    client.connect().subscribe({
      onComplete: socket => {
        socket.requestStream({data: '', metadata: ''}).subscribe({
          onComplete() {
            console.log('onComplete()');
          },
          onError(error) {
            console.log('onError(%s)', error.message);
          },
          onNext(payload) {
            self.handlePayload(payload);
            self.requestMoreDataIfNeeded();
          },
          onSubscribe(_subscription) {
            self.subscription = _subscription;
            console.log('on subscribe');
            _subscription.request(self.SINGLE_REQ);
          }
        });
      },
      onError: error => console.error(error),
      onSubscribe: cancel => {/* call cancel() to abort */
      },
      onNext: data => console.log(data)
    });


  }

  public pause() {
    this.paused = !this.paused;
    this.requestMoreDataIfNeeded();
  }


  private handlePayload(payload) {
    this.subject.next(payload.data);
    if (this.previous != null && (this.previous.id + 1) !== payload.data.id) {
      console.log('Missed ' + payload.data.id + ' previous ' + this.previous.id);
    }
    this.previous = payload.data;
  }

  private requestMoreDataIfNeeded() {
    if (!this.paused) {
      this.counter--;
      if (this.subscription != null && this.counter <= 0) {
        this.subscription.request(this.SINGLE_REQ);
        this.counter = this.SINGLE_REQ;
      }
    }
  }

  setSpeed(speed: number) {
    this.http.post(this.url + '/speed/' + speed, null).subscribe(() => console.log('Saved speed'));
  }
}
