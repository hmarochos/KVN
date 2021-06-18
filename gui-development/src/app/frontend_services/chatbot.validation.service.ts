import {Injectable} from '@angular/core';

@Injectable()
export default class ChatBotValidationService {

  QUESTION_MIN_LENGTH = 1;
  QUESTION_MAX_LENGTH = 250;

  QUESTION_REG_EXP_ALLOWED_CHARACTERS = '[a-zA-Zá-žÁ-Ž\\d\\w?!,.+¨*\\\\/|\\-\\s(){}\\[\\]\":]';
  QUESTION_REG_EXP = '^' + this.QUESTION_REG_EXP_ALLOWED_CHARACTERS + '{1,' + this.QUESTION_MAX_LENGTH + '}$';

  questionValidation(question) {
    const message = '';

    if (question === undefined || question === null) {
      return 'Fill in the question.';
    }

    const tmpQuestion = question.toString().trim();
    if (question.replace('\\s', '').isEmpty || question.toString().trim().length < this.QUESTION_MIN_LENGTH) {
      return 'The question must contain at least ' + this.QUESTION_MIN_LENGTH + ' characters.';
    }
    if (tmpQuestion.length > this.QUESTION_MAX_LENGTH) {
      return 'The question can contain a maximum of ' + this.QUESTION_MAX_LENGTH + 'characters.';
    }

    if (!tmpQuestion.match(this.QUESTION_REG_EXP)) {
      return 'The question contains illegal characters.';
    }

    return message;
  }

}
