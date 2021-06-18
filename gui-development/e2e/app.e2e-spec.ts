import {MoisFePage} from './app.po';

describe('mois-fe App', () => {
  let page: MoisFePage;

  beforeEach(() => {
    page = new MoisFePage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
