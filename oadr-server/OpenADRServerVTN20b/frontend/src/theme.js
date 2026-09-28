import { createTheme } from '@mui/material/styles';
// MUI 7 부터 package.json exports 가 한 단계 경로만 열어 준다. colors/indigo 처럼 깊게 들어가면 빌드가 막힌다
import { indigo, pink } from '@mui/material/colors';

// MUI 3 에는 ThemeProvider 가 없어도 기본 테마가 있었고 이 앱은 그 기본값에 기대어 그려져 있다.
// MUI 5 부터 기본값이 몇 가지 바뀌어서, 화면이 예전과 같게 보이도록 바뀐 것만 되돌린다.
//
//   색: MUI 3 기본 primary 는 indigo(#3f51b5), secondary 는 pink(#f50057) 였다.
//       MUI 5 부터 기본이 파랑(#1976d2), 보라(#9c27b0) 로 바뀌었다
//   입력창: MUI 3 의 TextField, Select 는 밑줄만 있는 standard 모양이 기본이었다.
//           MUI 5 부터 기본이 테두리 있는 outlined 로 바뀌었다
//   Grid: MUI 3 의 container 는 항상 width 100% 였다. MUI 7 부터 Grid 가 새 구현으로 바뀌면서
//         container 안에 바로 들어간 container 는 크기(size)가 없는 칸으로 취급되어 내용만큼 줄어든다.
//         이 앱은 container 안에 container 를 바로 넣는 곳이 많아서 입력칸이 좁게 몰렸다.
//         container 에 width 100% 를 다시 준다. size 를 준 container 는 이 앱에 없다
const theme = createTheme( {
  palette: {
    primary: { main: indigo[ 500 ] },
    secondary: { main: pink.A400 },
    // MUI 3 의 페이지 바탕은 grey[50](#fafafa) 였고 MUI 5 부터 흰색이다
    background: { default: '#fafafa' },
  },
  components: {
    MuiTextField: { defaultProps: { variant: 'standard' } },
    MuiSelect: { defaultProps: { variant: 'standard' } },
    MuiFormControl: { defaultProps: { variant: 'standard' } },
    MuiGrid: { styleOverrides: { container: { width: '100%' } } },
    // MUI 3 의 탭은 md 이상에서 최소 폭이 160px 이었다. MUI 5 부터 90px 이라 탭이 가운데로 몰린다
    MuiTab: {
      styleOverrides: {
        root: ( { theme } ) => ( { [ theme.breakpoints.up( 'md' ) ]: { minWidth: 160 } } ),
      },
    },
    // MUI 3 의 표 머리글은 작은 회색 글씨, 본문은 0.8125rem 이었다. MUI 5 부터 머리글이 진한 색에 본문이 조금 커졌다
    MuiTableCell: {
      styleOverrides: {
        head: ( { theme } ) => ( { color: theme.palette.text.secondary, fontSize: '0.75rem' } ),
        body: { fontSize: '0.8125rem' },
      },
    },
  },
} );

export default theme;
