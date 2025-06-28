const { test, expect } = require('@playwright/test');

async function login(page) {
  await page.goto('http://localhost:8080?controller=auth&action=login');
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'pass');
  await Promise.all([
    page.waitForNavigation(),
    page.click('input[type="submit"]')
  ]);
}

test.beforeEach(async ({ browser }) => {
  console.log('Resetting the application state');
  const page = await browser.newPage();
  await login(page);
  await page.goto('http://localhost:8080?controller=admin&action=reset');
  await page.close();
});

test('homepage has expected title', async ({ page }) => {
  await page.goto('http://localhost:8080');
  await expect(page).toHaveTitle(/Pheide/);
});

test('general navigation switches tab content', async ({ page }) => {
  await page.goto('http://localhost:8080');

  console.log('Checking initial tab content');
  const tabContent1 = await page.locator('#tabContent').innerText();
  expect(tabContent1.trim()).toBe('Main content 1');

  console.log('Clicking #mill tab link');
  await page.hover('#mill');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#mill a')
  ]);

  console.log('Checking tab content after navigation');
  const tabContent2 = await page.locator('#tabContent').innerText();
  expect(tabContent2.trim()).toBe('mill tab content 1');
});

test('general content editing updates tab content', async ({ page }) => {
  console.log('Logging in');
  await login(page);

  console.log('Navigating to main page');
  await page.goto('http://localhost:8080');

  console.log('Filling in new content in textarea');
  console.log('Double clicking #tabContent to enter edit mode');
  await page.dblclick('#tabContent');

  console.log('Filling in new content in textarea');
  const newContent = 'Edited main content!';
  await page.fill('#tabContent_edit textarea', newContent);

  console.log('Submitting edited content');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#tabContent_edit input[type="submit"]')
  ]);

  console.log('Verifying updated content');
  const updatedContent = await page.locator('#tabContent').innerText();
  expect(updatedContent.trim()).toBe(newContent);
});

test('can create and delete a new tab', async ({ page }) => {
  console.log('Logging in');
  await login(page);

  console.log('Navigating to main page');
  await page.goto('http://localhost:8080');

  console.log('Clicking "+" to add a new tab');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#cats li a[href*="controller=tab&action=new"]')
  ]);

  console.log('Filling in new tab');
  const newTabTitle = 'My New Tab';
  await page.fill('#cats li.activeTab input[name="title"]', newTabTitle);

  console.log('Submitting new tab form');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#cats li.activeTab input[type="submit"]')
  ]);

  console.log('Verifying the active tab is the new one');
  const activeTabText = await page.locator('#cats li.activeTab #tabTitle').innerText();
  expect(activeTabText).toContain(newTabTitle);

  console.log('Clicking the delete (x) button on the new tab');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#cats li.activeTab a.tabAction[href*="action=delete"]')
  ]);

  console.log('Verifying new tab is removed from tab list');
  const tabListAfterDelete = await page.locator('#cats').innerText();
  expect(tabListAfterDelete).not.toContain(newTabTitle);
});

test('can reorder tabs by shifting right', async ({ page }) => {
  console.log('Logging in');
  await login(page);

  console.log('Navigating to main page');
  await page.goto('http://localhost:8080');

  console.log('Checking there are at least two tabs');
  const tabItems = await page.locator('#cats > li').all();
  expect(tabItems.length).toBeGreaterThanOrEqual(2);

  console.log('Verifying the active tab is the first one');
  const firstTab = tabItems[0];
  const activeTab = await page.locator('#cats li.activeTab');
  const activeTabText = await activeTab.innerText();
  const firstTabText = await firstTab.innerText();
  expect(activeTabText.trim()).toBe(firstTabText.trim());

  console.log('Clicking ">" to shift active tab right');
  await Promise.all([
    page.waitForNavigation(),
    activeTab.locator('a.tabAction[href*="direction=right"]').click()
  ]);

  console.log('Verifying the active tab is now the second tab and unchanged');
  const updatedTabItems = await page.locator('#cats > li').all();
  const newActiveTab = await page.locator('#cats li.activeTab');
  const newActiveTabText = await newActiveTab.innerText();
  const secondTabText = await updatedTabItems[1].innerText();
  expect(newActiveTabText.trim()).toBe(activeTabText.trim());
  expect(secondTabText.trim()).toBe(activeTabText.trim());
});

test('can create and delete a new page', async ({ page }) => {
  console.log('Logging in');
  await login(page);

  console.log('Navigating to main page');
  await page.goto('http://localhost:8080');

  console.log('Hovering and clicking to create notebook page');
  await page.hover('#notebook');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#notebook a')
  ]);

  console.log('Verifying page title is notebook');
  const pageTitle = await page.locator('#current #pageTitle').innerText();
  expect(pageTitle.trim()).toBe('notebook');

  console.log('Clicking delete (x) button for notebook page');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#current #page_delete_button')
  ]);

  console.log('Verifying notebook page was deleted, i.e. link is now a create link');
  await page.hover('#notebook');
  const notebookHref = await page.getAttribute('#notebook a', 'href');
  expect(notebookHref).toContain('controller=page&action=create');
});

test('can set a page as default', async ({ page }) => {
  console.log('Logging in');
  await login(page);

  console.log('Navigating to base page');
  await page.goto('http://localhost:8080');
  let pageTitle = await page.locator('#current #pageTitle').innerText();
  expect(pageTitle.trim()).toBe('Home');

  console.log('Navigating to mill page');
  await page.hover('#mill');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#mill a')
  ]);
  pageTitle = await page.locator('#current #pageTitle').innerText();
  expect(pageTitle.trim()).toBe('Hobby');

  console.log('Double clicking page title to edit');
  await page.dblclick('#current #pageTitle');

  console.log('Checking the "default?" checkbox');
  await page.check('#current #pageTitle_edit input[name="is_default"]');

  console.log('Submitting the form to set as default');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#current #pageTitle_edit input[type="submit"]')
  ]);

  console.log('Navigating to base URL to verify default page');
  await page.goto('http://localhost:8080');
  pageTitle = await page.locator('#current #pageTitle').innerText();
  expect(pageTitle.trim()).toBe('Hobby');
});
