import {Component, OnInit} from '@angular/core';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import ChatBotValidationService from '../frontend_services/chatbot.validation.service';
import ChatBotService from '../backend_services/chatbot.service';
import * as moment from 'moment';
declare var $: any;

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css'],
  providers: [CookieService, ChatBotValidationService, ChatBotService]
})
// TODO - Error s časem, občas hodí chybu - fixnout!
export default class ChatBotComponent implements OnInit {

  audio;
  message;
  messageList = [];
  messageWait = false;
  showHideSendButton = false;
  timeNow;

  constructor(private _cookieService: CookieService, private _chatBotValidationService: ChatBotValidationService,
              private _chatBotService: ChatBotService) {
  }


  getCurrTime() {
    return moment().format('HH:mm');
  }


  sendMessage(message) {
    const validationMessage = this._chatBotValidationService.questionValidation(message);
    if (validationMessage.length === 0) {
      this.showHideSendButton = true;
      this.message = '';
      // ID:0 pro uživatele, ID:1 pro server
      this.messageList.push({message: message, id: 0, time: this.getCurrTime()});

      const dtoIn = {
        question: message
      };

      this._chatBotService.sendMessageToBot(dtoIn).then(resp => {
        this.writeMessage(resp.data.body.answer);
      }).catch(() => {
        this.writeError();
      });
    } else {
      alert(validationMessage);
    }
  }


  writeMessage(message) {
    this.messageWait = true;
    sessionStorage.setItem('chat-bot', JSON.stringify(this.messageList));
    sessionStorage.setItem('chat-bot-time', JSON.stringify(this.timeNow));
    setTimeout(() => {
        this.messageList.push({message: message, id: 1, time: this.getCurrTime()});
        this.messageWait = false;
        sessionStorage.setItem('chat-bot', JSON.stringify(this.messageList));
        this.showHideSendButton = false;
        this.alertNewMessage();
        $('.chat-history').animate({scrollTop: 20000000}, 'slow');
      },
      Math.floor(Math.random() * (3000 - 50)) + 50);

  }


  writeError() {
    this.messageList.push({message: 'An error occurred on the server', id: -1, time: this.getCurrTime()});
    this.alertNewMessage();
    setTimeout(() => {
        this.showHideSendButton = false;
      },
      1000);
  }


  chatToggler() {
    $('.chat').slideToggle(300, 'swing');
    $('.chat-message-counter').fadeToggle(300, 'swing');
  }


  playAudio() {
    this.audio.load();
    this.audio.play();
  }


  ngOnInit(): void {
    this.audio = new Audio();
    this.audio.src = '/src/assets/sounds/hollow.ogg';
    this.audio.volume = 0.4;

    const input = document.getElementById('message');
    input.addEventListener('keyup', function (event) {
      if (event.keyCode === 13) {
        event.preventDefault();
        document.getElementById('messageSend').click();
      }
    });

    if (sessionStorage.getItem('chat-bot') !== null) {
      this.messageList = JSON.parse(sessionStorage.getItem('chat-bot'));
    }

    if (sessionStorage.getItem('chat-bot-time') !== null) {
      this.timeNow = JSON.parse(sessionStorage.getItem('chat-bot-time'));
    } else {
      this.timeNow = this.getCurrTime();
    }

    setTimeout(() => {
        if ($('.chat').css('display') === 'none' && sessionStorage.getItem('chat-bot') === null) {
          this.chatToggler();
          this.playAudio();
        }
      },
      2000);
  }


  alertNewMessage() {
    this.playAudio();
    if ($('.chat').css('display') === 'none') {
      this.chatToggler();
    }
  }
}
